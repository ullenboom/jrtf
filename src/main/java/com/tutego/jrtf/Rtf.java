/*
 * Copyright (c) 2010-2026 Christian Ullenboom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jRTF' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tutego.jrtf;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.*;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import static java.nio.charset.CodingErrorAction.REPORT;

/**
 * Main class to build and stream a RTF document.
 * <p>
 * Example how to generate a RTF file:
 * <pre>
 * Rtf.rtf().p( "Hello", bold("RTF") ).out( new FileWriter("out.rtf") );
 * </pre>
 */
public class Rtf {
  /**
   * Charset used for converting chars in the range of 127 < x < 255.
   */
  final static Charset charset = Charset.forName( "Windows-1252" );
  final static CharsetEncoder charsetEncoder = charset.newEncoder().onMalformedInput( REPORT )
                                                      .onUnmappableCharacter( REPORT );
  final static String CHARSET1252 = charset.name();

  /**
   * Associates an index with a color.
   */
  private final SortedMap<Integer, RtfHeaderColor> headerColors = new TreeMap<>();

  /**
   * List of fonts.
   */
  private final List<RtfHeaderFont> headerFonts = new ArrayList<>();

  /**
   * List of style sheets.
   */
  private final List<RtfHeaderStyle> headerStyles = new ArrayList<>();

  /**
   * List definitions ({@code \listtable} / {@code \listoverridetable}).
   */
  private final List<RtfList> lists = new ArrayList<>();

  /**
   * Table style definitions.
   */
  private final List<RtfTableStyle> tableStyles = new ArrayList<>();

  /**
   * Document info renderers.
   */
  private final List<Consumer<RtfOutput>> infoRenderers = new ArrayList<>();

  /**
   * Document format renderers.
   */
  private final List<Consumer<RtfOutput>> docfmtRenderers = new ArrayList<>();

  /**
   * Document variables (key-value pairs for mail-merge / metadata).
   */
  private final Map<String, String> documentVariables = new LinkedHashMap<>();

  /**
   * Section data will be stored in 2 lists: One for the section formatting and headers
   */
  private final List<@Nullable Consumer<RtfOutput>> secfmtHdrftrRenderers = new ArrayList<>();
  /**
   * and another list for the paragraphs itself.
   */
  private final List<RtfPara[]> sectionParagraphs = new ArrayList<>();

  /**
   * Private constructor. The user will not instantiate this class.
   */
  private Rtf() {}

  /**
   * Creates a new RTF document.
   *
   * @return The new RTF document.
   */
  public static Rtf rtf() {
    return new Rtf();
  }

  /**
   * Converts a given char sequence into RTF format and writes it to the output buffer.
   *
   * @param out     Output buffer.
   * @param rawText Raw text to escape.
   */
  static void asRtf( RtfOutput out, String rawText ) {
    for ( int i = 0; i < rawText.length(); i++ ) {
      char c = rawText.charAt( i );

      if ( c == '\n' )
        out.ctrl( RtfControlWords.PAR );
      else if ( c == '\t' )
        out.ctrl( RtfControlWords.TAB );
      else if ( c == '\\' )
        out.append( "\\\\" );
      else if ( c == '{' )
        out.append( "\\{" );
      else if ( c == '}' )
        out.append( "\\}" );
      else if ( c < 127 )
        out.append( c );
      else { // Use Unicode and ask the char from the String object; control word takes a signed 16-bit value per spec
        out.cw( RtfControlWords.UNICODE_CHAR ).append( (short) c );
        // Unicode fallback: how many chars to skip if the unicode control word is not understood
        out.cw( RtfControlWords.UNICODE_FALLBACK ).append( "1" );
        out.append( escapeWindows1252( c ) );
      }
    }
  }

  /**
   * Converts a given string to an encoded RTF sting. A new line character will be converted to \par.
   *
   * @param rawText Raw text. May be {@code null}, in which case {@code null} is returned.
   * @return RTF encoded string, or {@code null} if {@code rawText} was {@code null}.
   */
  static @Nullable String asRtf( @Nullable String rawText ) {
    if ( rawText == null )
      return null;

    StringBuilder result = new StringBuilder( rawText.length() * 2 );
    asRtf( new RtfOutput( result ), rawText );
    return result.toString();
  }

  /**
   * Sets RTF headers for the document.
   *
   * @param headers Sequence of headers.
   * @return {@code this}-reference.
   */
  public Rtf header( RtfHeader... headers ) {
    for ( RtfHeader rtfHeader : headers ) {
      if ( rtfHeader instanceof RtfHeaderColor )
        headerColors.put( ((RtfHeaderColor) rtfHeader).colorindex, (RtfHeaderColor) rtfHeader );
      else if ( rtfHeader instanceof RtfHeaderFont )
        headerFonts.add( (RtfHeaderFont) rtfHeader );
    }

    return this;
  }

  /**
   * Writes stylesheet group, which contains information about styles used in the document.
   *
   * @param styles RTF style sheet objects.
   * @return {@code this}-reference.
   */
  public Rtf headerStyles( RtfHeaderStyle... styles ) {
    for ( RtfHeaderStyle rtfStyle : styles ) {
      if ( headerStyles.contains( rtfStyle ) )
        continue;

      int nextId = 0;
      for ( RtfHeaderStyle registered : headerStyles )
        nextId = Math.max( nextId, registered.getId() + 1 );

      rtfStyle.assignIdIfUnassigned( nextId );
      headerStyles.add( rtfStyle );
    }

    return this;
  }

  /**
   * Registers list definitions, so paragraphs can reference them with
   * {@link RtfTextPara#list(RtfList, int)}. Registering the same list twice has no effect.
   *
   * @param lists Lists to register. Must not be {@code null}.
   * @return {@code this}-reference.
   */
  public Rtf lists( RtfList... lists ) {
    for ( RtfList list : lists ) {
      if ( this.lists.contains( list ) )
        continue;

      list.overrideIndex = this.lists.size() + 1;
      this.lists.add( list );
    }

    return this;
  }

  /**
   * Registers table style definitions, so rows can reference them with
   * {@link RtfRow#tableStyle(RtfTableStyle)}.
   *
   * @param styles Table styles to register.
   * @return {@code this}-reference.
   */
  public Rtf tableStyles( RtfTableStyle... styles ) {
    for ( RtfTableStyle style : styles ) {
      if ( tableStyles.contains( style ) )
        continue;
      style.assignIdIfUnassigned( tableStyles.size() );
      tableStyles.add( style );
    }
    return this;
  }

  /**
   * Writes information group, which contains information about the document.
   * This can include the title, author, keywords, comments, and other information
   * specific to the file. This information is for use by a document-management utility.
   *
   * @param infos RTF info objects.
   * @return {@code this}-reference.
   */
  public Rtf info( RtfInfo... infos ) {
    for ( RtfInfo rtfInfo : infos )
      infoRenderers.add( rtfInfo::rtf );

    return this;
  }

  /**
   * Document formatting to specify the attributes of the document,
   * such as pager with, margins or footnote placement.
   *
   * @param documentFormattings RTF document formattings.
   * @return {@code this}-reference.
   */
  /**
   * Sets a document variable (key-value pair stored in the RTF file).
   * Useful for mail-merge templates and external document processing tools.
   *
   * @param key   Variable name (ASCII, no spaces).
   * @param value Variable value.
   * @return {@code this}-reference.
   */
  public Rtf docvar( String key, String value ) {
    if ( key == null || key.isEmpty() )
      throw new IllegalArgumentException( "Document variable key must not be empty" );
    documentVariables.put( key, value == null ? "" : value );
    return this;
  }

  public Rtf documentFormatting( RtfDocfmt... documentFormattings ) {
    for ( RtfDocfmt rtfDocfmt : documentFormattings )
      docfmtRenderers.add( rtfDocfmt::rtf );

    return this;
  }

  /**
   * Creates a new section with paragraphs and appends them to the RTF document.
   *
   * @param paragraphs Paragraphs.
   * @return {@code this}-reference.
   */
  public Rtf section( RtfPara... paragraphs ) {
    return section( null, paragraphs );
  }

  /**
   * Creates a new section with paragraphs and appends them to the RTF document.
   *
   * @param paragraphs Paragraphs.
   * @return {@code this}-reference.
   */
  public Rtf section( Collection<RtfPara> paragraphs ) {
    return section( null, paragraphs.toArray( new RtfPara[ paragraphs.size() ] ) );
  }

  /**
   * Creates a new formatted section with paragraphs and appends them to the RTF document.
   *
   * @param secfmtHdrftr Formattings. May be {@code null} (the section gets no extra formatting).
   * @param paragraphs   Paragraphs to be written. Must not be {@code null}.
   * @return {@code this}-reference.
   */
  public Rtf section( @Nullable RtfSectionFormatAndHeaderFooter secfmtHdrftr, RtfPara... paragraphs ) {
    if ( paragraphs == null )
      throw new IllegalArgumentException( "There has to be atleast one paragraph in a section" );

    // First add the style

    if ( secfmtHdrftr != null )
      secfmtHdrftrRenderers.add( secfmtHdrftr::rtf );
    else
      secfmtHdrftrRenderers.add( null );

    // then the paragraphs itself to the second list

    sectionParagraphs.add( paragraphs );

    return this;
  }

  /**
   * Appends a sequence of text in a new paragraph to the RTF document.
   * A convenience method which is equals to {@code section(RtfPara.p(texts));}.
   *
   * @param texts Text to put in paragraph.
   * @return {@code this}-reference.
   */
  public Rtf p( Object... texts ) {
    return section( RtfPara.p( texts ) );
  }

  /**
   * Appends a sequence of text in a new paragraph to the RTF document.
   * A convenience method which is equals to {@code section(RtfPara.p( style, texts));}.
   *
   * @param style Style sheet to set in paragraph.
   * @param texts Text to put in paragraph.
   * @return {@code this}-reference.
   */
  public Rtf p( RtfHeaderStyle style, Object... texts ) {
    return section( RtfPara.p( style, texts ) );
  }

  /**
   * Writes the RTF document and send the output to an {@link Appendable}.
   * This method closes the {@link Appendable} after writing if it is of type
   * {@link Closeable}.
   *
   * @param out Destination of this RTF output. Must not be {@code null}.
   * @throws IllegalArgumentException if {@code out} is {@code null}.
   */
  public void out( Appendable out ) {
    if ( out == null )
      throw new IllegalArgumentException( "Appendable is not allowed to be null" );

    try ( @Nullable Closeable closeable = out instanceof Closeable ? (Closeable) out : null ) {
      writeRtfDocument( new RtfOutput( out ) );
    }
    catch ( IOException e ) {
      throw new RtfException( e );
    }
  }

  /**
   * Returns the RTF document as a {@link CharSequence}.
   *
   * @return The RTF document.
   */
  public CharSequence out() {
    StringBuilder result = new StringBuilder( 4096 );
    writeRtfDocument( new RtfOutput( result ) );
    return result.toString();
  }

  /**
   * Returns the RTF document as a String.
   *
   * @return The RTF document.
   */
  @Override
  public String toString() {
    return out().toString();
  }

  /**
   * Opens a RTF template for later variable substitution.
   *
   * @param inputStream Source of the RTF file.
   * @return Template object to make the substitutions on.
   */
  public static RtfTemplate template( InputStream inputStream ) {
    return new RtfTemplate( inputStream );
  }

  /**
   * Writes the complete RTF document.
   */
  private void writeRtfDocument( RtfOutput out ) {
    /*
     * <File>     := '{' <header> <document>'}'
     * <header>   := \rtf <charset> \deff? <fonttbl> <colortbl> <stylesheet>?
     * <document> := <info>? <docfmt>* <section>+
     * <section>  := <secfmt>* <hdrftr>? <para>+ ( \sect <section>)?
     */

    // Write <header>

    /*
     * <header>   := \rtf
     *               <charset>
     *               <deffont>
     *               \deff?
     *               <fonttbl>
     *               <filetbl>?
     *               <colortbl>?
     *               <stylesheet>?
     *               <listtables>?
     *               <revtbl>?
     *               <rsidtable>?
     *               <generator>?
     */

    out.open();   // '{' <header> <document>'}'

    // The RTF version will always be 1 and the
    // character is \ansi = Windows 1252

    out.cw( RtfControlWords.RTF_VERSION ).append( "1" )
       .cw( RtfControlWords.ANSI_CHARSET ).cw( RtfControlWords.DEFAULT_FONT ).append( "0" );

    /*
     * <fonttbl>  := '{' \fonttbl (<fontinfo> | ('{' <fontinfo> '}'))+ '}'
     */
    out.nl().open( RtfControlWords.FONT_TABLE );

    if ( headerFonts.isEmpty() )
      out.entry( RtfControlWords.FONT, "0 Times New Roman" );
    else {
      for ( RtfHeaderFont font : headerFonts )
        font.writeFontInfo( out );
    }

    out.close();

    /*
     * <colortbl> := '{' \colortbl <colordef>+ '}'
     */
    if ( !headerColors.isEmpty() ) {
      out.nl().open( RtfControlWords.COLOR_TABLE );

      int maxColorIndex = headerColors.lastKey();

      for ( int i = 0; i <= maxColorIndex; i++ ) {
        RtfHeaderColor color = headerColors.get( i );
        if ( color == null )
          out.semi();
        else
          color.writeColordef( out );
      }

      out.close();
    }
    else
      out.nl().tag( RtfControlWords.COLOR_TABLE, ";" );

    /*
     * <stylesheet> := '{' \ stylesheet <style>+ '}'
     */

    if ( !headerStyles.isEmpty() ) {
      out.nl().open( RtfControlWords.STYLE_SHEET );
      for ( RtfHeaderStyle style : headerStyles )
        out.append( style.toString() );

      out.close();
    }

    /*
     * <listtables> := <listtable> <listoverridetable>
     */
    if ( !lists.isEmpty() ) {
      out.nl().open( RtfControlWords.LIST_TABLE_DESTINATION ).nl();
      for ( RtfList list : lists )
        list.writeListDefinition( out );
      out.close().nl().open( RtfControlWords.LIST_OVERRIDE_TABLE_DESTINATION ).nl();
      for ( RtfList list : lists )
        list.writeListOverride( out );
      out.close();
    }

    // Write table styles

    if ( !tableStyles.isEmpty() ) {
      for ( RtfTableStyle ts : tableStyles )
        ts.rtf( out );
      out.nl();
    }

    out.nl();

    // Write <info>

    if ( !infoRenderers.isEmpty() ) {
      out.open( RtfControlWords.INFO_DESTINATION );
      for ( Consumer<RtfOutput> infoRenderer : infoRenderers )
        infoRenderer.accept( out );
      out.close().nl();
    }

    // Write <docfmt>

    for ( Consumer<RtfOutput> docfmtRenderer : docfmtRenderers )
      docfmtRenderer.accept( out );

    // Write document variables

    if ( !documentVariables.isEmpty() ) {
      for ( Map.Entry<String, String> var : documentVariables.entrySet() ) {
        out.open( RtfControlWords.DOCUMENT_VARIABLE ).sp()
           .append( var.getKey() ).sp()
           .append( Rtf.asRtf( var.getValue() ) )
           .close().nl();
      }
    }

    /*
     * <document> := <info>? <docfmt>* <section>+
     * <section>  := <secfmt>* <hdrftr>? <para>+ ( \sect <section>)?
     */

    for ( int sectionCnt = 0; sectionCnt < sectionParagraphs.size(); sectionCnt++ ) {
      RtfPara[] paragraphs = sectionParagraphs.get( sectionCnt );
      @Nullable Consumer<RtfOutput> secfmtHdrftrRenderer = secfmtHdrftrRenderers.get( sectionCnt );

      // <secfmt>* <hdrftr>?

      if ( secfmtHdrftrRenderer != null )
        secfmtHdrftrRenderer.accept( out );

      // <para>+

      for ( RtfPara rtfPara : paragraphs )
        rtfPara.rtf( out, true );

      // write \sect between sections but not at the end

      if ( sectionCnt != sectionParagraphs.size() - 1 )
        out.ctrl( RtfControlWords.SECTION );
    }

    // We are done

    out.close();
  }

  /**
   * Escape character with <code>\'xx</code> type escaping using windows-1252 encoding.
   */
  static String escapeWindows1252( char c ) {
    if ( !charsetEncoder.canEncode( c ) ) {
      return "?";
    }

    try {
      final ByteBuffer bytes = charsetEncoder.encode( CharBuffer.wrap( String.valueOf( c ) ) );
      final int unsignedCharByte = bytes.get() & 255; // Treat byte as unsigned
      return BYTE_HEX_LOOKUP[ unsignedCharByte ];
    }
    catch ( CharacterCodingException err ) {
      throw new RtfException( err );
    }
  }

  private static final String[] BYTE_HEX_LOOKUP = new String[ 256 ];
  static {
    for ( int i = 0; i < 256; i++ )
      BYTE_HEX_LOOKUP[ i ] = String.format( "\\'%02x", i );
  }
}
