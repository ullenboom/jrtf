/*
 * Copyright (c) 2010-2015 Christian Ullenboom
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

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

import static java.nio.charset.CodingErrorAction.REPORT;

/**
 * Main class to build and stream a RTF document.
 * <p>
 * Example how to generate a RTF file:
 * <pre>
 * Rtf.rtf().p( "Hello", bold("RTF") ).out( new FileWriter("out.rtf") );
 * </pre>
 */
public class Rtf
{
  /** Charset used for converting chars in the range of 127 < x < 255. */
  final static Charset charset = Charset.forName("Windows-1252");
  final static CharsetEncoder charsetEncoder = charset.newEncoder().onMalformedInput(REPORT).onUnmappableCharacter(REPORT);
  final static String CHARSET1252 = charset.name();

  /** Associates an index with a color. */
  private SortedMap<Integer, RtfHeaderColor> headerColors = new TreeMap<Integer, RtfHeaderColor>();

  /** List of fonts. */
  private List<RtfHeaderFont> headerFonts = new ArrayList<RtfHeaderFont>();

  /** List of style sheets. */
  private List<RtfHeaderStyle> headerStyles = new ArrayList<RtfHeaderStyle>();

  /** Document info. */
  private StringBuilder info = new StringBuilder();

  /** Document format. */
  private StringBuilder docfmt = new StringBuilder();

  /** Section data will be stored in 2 lists: One for the section formatting and headers */
  private List<CharSequence> secfmtHdrftrs = new ArrayList<CharSequence>();
  /** and another list for the paragraphs itself. */
  private List<RtfPara[]> sectionParagraphs = new ArrayList<RtfPara[]>();

  /**
   * Private constructor. The user will not instantiate this class.
   */
  private Rtf() { }

  /**
   * Creates a new RTF document.
   * @return The new RTF document.
   */
  public static Rtf rtf()
  {
    return new Rtf();
  }

  /**
   * Converts a given char sequence into RTF format and stream it to the {@code Appendable}.
   * @param out
   * @param rawText
   * @throws IOException 
   */
  static void asRtf( Appendable out, String rawText ) throws IOException
  {
    for ( int i = 0; i < rawText.length(); i++ )
    {
      char c = rawText.charAt( i ); 

      if ( c == '\n' )
        out.append( "\\par\n" );
      else if ( c == '\t' )
        out.append( "\\tab\n" );
      else if ( c == '\\' )
        out.append( "\\\\" );
      else if ( c == '{' )
        out.append( "\\{" );
      else if ( c == '}' )
        out.append( "\\}" );
      else if ( c < 127 )
        out.append( c );
      else   // Use Unicode and ask the char from the String object
        out.append( "\\u" ).append( Integer.toString( c ) ).append( escapeWindows1252(c) );
    }
  }

  /**
   * Converts a given string to an encoded RTF sting. A new line character will be converted to \par.
   * @param rawText Raw text.
   * @return RTF encoded string.
   */
  static String asRtf( String rawText )
  {
    if ( rawText == null )
      return null;

    // TODO: Optimize

    StringBuilder result = new StringBuilder( rawText.length() * 2 );
    try
    {
      asRtf( result, rawText );
    }
    catch ( IOException e )
    {
      // If this will happen we are really in trouble
      throw new RtfException( e );
    }
    return result.toString();
  }

  /**
   * Sets RTF headers for the document.
   * @param headers Sequence of headers.
   * @return {@code this}-reference.
   */
  public Rtf header( RtfHeader... headers )
  {
    for ( RtfHeader rtfHeader : headers )
    {
      if ( rtfHeader instanceof RtfHeaderColor )
        headerColors.put( ((RtfHeaderColor) rtfHeader).colorindex, (RtfHeaderColor) rtfHeader );
      else if ( rtfHeader instanceof RtfHeaderFont )
        headerFonts.add( (RtfHeaderFont) rtfHeader );
//      else if ( rtfHeader instanceof RtfHeaderStyle )
//        headerStyles.add( (RtfHeaderStyle) rtfHeader );
    }

    return this;
  }

  /**
   * Writes stylesheet group, which contains information about styles used in the document.
   * @param styles RTF style sheet objects.
   * @return {@code this}-reference.
   */
  public Rtf headerStyles( RtfHeaderStyle... styles )
  {
    for  (RtfHeaderStyle rtfStyle : styles )
      if ( ! headerStyles.contains( rtfStyle ) )
        headerStyles.add( rtfStyle );

    return this;
  }

  /**
   * Writes information group, which contains information about the document.
   * This can include the title, author, keywords, comments, and other information
   * specific to the file. This information is for use by a document-management utility.
   * @param infos  RTF info objects.
   * @return {@code this}-reference.
   */
  public Rtf info( RtfInfo... infos )
  {
    for ( RtfInfo rtfInfo : infos )
      info.append( rtfInfo.rtf );

    return this;
  }

  /**
   * Document formatting to specify the attributes of the document,
   * such as pager with, margins or footnote placement.
   * @param documentFormattings RTF document formattings.
   * @return {@code this}-reference.
   */
  public Rtf documentFormatting( RtfDocfmt... documentFormattings )
  {
    for ( RtfDocfmt rtfDocfmt : documentFormattings )
      docfmt.append( rtfDocfmt.rtf );

    return this;
  }

  /**
   * Creates a new section with paragraphs and appends them to the RTF document.
   * @param paragraphs Paragraphs.
   * @return {@code this}-reference.
   */
  public Rtf section( RtfPara... paragraphs )
  {
    return section( null, paragraphs );
  }

  /**
   * Creates a new section with paragraphs and appends them to the RTF document.
   * @param paragraphs Paragraphs.
   * @return {@code this}-reference.
   */
  public Rtf section( Collection<RtfPara> paragraphs )
  {
    return section( null, paragraphs.toArray( new RtfPara[paragraphs.size()] ) );
  }

  /**
   * Creates a new formatted section with paragraphs and appends them to the RTF document.
   * @param secfmtHdrftr Formattings. Might be {@code null}.
   * @param paragraphs  Paragraphs to be written.
   * @return {@code this}-reference.
   */
  public Rtf section( RtfSectionFormatAndHeaderFooter secfmtHdrftr, RtfPara... paragraphs )
  {
    if ( paragraphs == null )
      throw new IllegalArgumentException( "There has to be atleast one paragraph in a section" );

    // First add the style

    if ( secfmtHdrftr != null )
      secfmtHdrftrs.add( secfmtHdrftr.rtf );
    else
      secfmtHdrftrs.add( null );

    // then the paragraphs itself to the second list

    sectionParagraphs.add( paragraphs );

    return this;
  }

  /**
   * Appends a sequence of text in a new paragraph to the RTF document.
   * A convenience method which is equals to {@code section(RtfPara.p(texts));}.
   * @param texts Text to put in paragraph.
   * @return {@code this}-reference.
   */
  public Rtf p( Object... texts )
  {
    return section( RtfPara.p( texts ) );
  }

  /**
   * Appends a sequence of text in a new paragraph to the RTF document.
   * A convenience method which is equals to {@code section(RtfPara.p( style, texts));}.
   * @param style Style sheet to set in paragraph.
   * @param texts Text to put in paragraph.
   * @return {@code this}-reference.
   */
  public Rtf p( RtfHeaderStyle style,  Object... texts )
  {
    return section( RtfPara.p( style, texts ) );
  }

  /**
   * Writes the RTF document and send the output to an {@link Appendable}.
   * This method closes the {@link Appendable} after writing if its of type
   * {@link Closeable}.
   * @param out Destination of this RTF output.
   */
  public void out( Appendable out )
  {
    if ( out == null )
      throw new IllegalArgumentException( "Appendable is not allowed to be null" );

    try
    {
      writeRtfDocument( out );
    }
    catch ( IOException e )
    {
      throw new RtfException( e );
    }
    finally
    {
      if ( out instanceof Closeable )
        try { ((Closeable) out).close(); } catch ( IOException e ) { throw new RtfException(e); }
    }
  }

  /**
   * Returns the RTF document as a {@link CharSequence}.
   * @return The RTF document.
   */
  public CharSequence out()
  {
    StringBuilder result = new StringBuilder( 4096 );
    out( result );
    return result;
  }

  /**
   * Returns the RTF document as a String.
   * @return The RTF document.
   */
  @Override
  public String toString()
  {
    return out().toString();
  }

  /**
   * Opens a RTF template for later variable substitution.
   * @param  inputStream Source of the RTF file.
   * @return Template object to make the substitutions on.
   */
  public static RtfTemplate template( InputStream inputStream )
  {
    return new RtfTemplate( inputStream );
  }

  /**
   * Writes the complete RTF document.
   */
  private void writeRtfDocument( Appendable out ) throws IOException
  {
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

    out.append( "{" );   // '{' <header> <document>'}'

    // The RTF version will always be 1 and the
    // character is \ansi = Windows 1252

    out.append( "\\rtf1\\ansi\\deff0" );

    /*
     * <fonttbl>  := '{' \fonttbl (<fontinfo> | ('{' <fontinfo> '}'))+ '}'
     */
    out.append( "\n{\\fonttbl" );

    if ( headerFonts.isEmpty() )
      out.append( "{\\f0 Times New Roman;}" );
    else
    {
      for ( RtfHeaderFont font : headerFonts )
        font.writeFontInfo( out );
    }

    out.append( '}' );

    /*
     * <colortbl> := '{' \colortbl <colordef>+ '}'
     */
    if ( ! headerColors.isEmpty() )
    {
      out.append( "\n{\\colortbl" );

      int maxColorIndex = headerColors.lastKey().intValue();

      for ( int i = 0; i <= maxColorIndex; i++ )
      {
        RtfHeaderColor color = headerColors.get( i );
        if ( color == null )
          out.append( ';' );
        else
          color.writeColordef( out );
      }

      out.append( '}' );
    } else
    	out.append( "\n{\\colortbl;}" );

    /*
     * <stylesheet> := '{' \ stylesheet <style>+ '}'
     */

    if ( ! headerStyles.isEmpty() )
    {
      out.append( "\n{\\stylesheet" );
      for ( RtfHeaderStyle style : headerStyles )
    	out.append( style.toString() );

      out.append( '}' );
    }

    out.append( '\n' );

    // Write <info>

    if ( info.length() > 0 )
    {
      out.append( "{\\info" );
      out.append( info );
      out.append( "}\n" );
    }

    // Write <docfmt>

    if ( docfmt.length() > 0 )
      out.append( docfmt );

    /*
     * <document> := <info>? <docfmt>* <section>+
     * <section>  := <secfmt>* <hdrftr>? <para>+ ( \sect <section>)?
     */

    for ( int sectionCnt = 0; sectionCnt < sectionParagraphs.size(); sectionCnt++ )
    {
      RtfPara[]    paragraphs   = sectionParagraphs.get( sectionCnt );
      CharSequence secfmtHdrftr = secfmtHdrftrs.get( sectionCnt );

      // <secfmt>* <hdrftr>?

      if ( secfmtHdrftr != null )
        out.append( secfmtHdrftr );

      // <para>+

      for ( RtfPara rtfPara : paragraphs )
        rtfPara.rtf( out, true );

      // write \sect between sections but not at the end

      if ( sectionCnt != sectionParagraphs.size() - 1 )
        out.append( "\\sect\n" );
    }

    //TODO:
    //        // Place \par between paragraphs not at the end of every paragraph.
    //        // Consequence: If there is just one paragraph there isn't any \par
    //        if ( i > 0 )
    //        {
    //          // Just add \par if the one before is a paragraph too.
    //          // we don't want \par between table rows
    //          if (    paragraphs[i-1] instanceof RtfTextPara
    //               && paragraphs[ i ] instanceof RtfTextPara )
    //            out.append( "\\par\n" );
    //        }

    // We are done

    out.append( "}" );
  }

  // Internal utility methods

  /**
   * Frames a {@link RtfPara}. The result is similar too
   * <code>"{\" + rtfControlWord + " " + RTF of para + "}"</code>.
   */
  static StringBuilder frameRtfParagraphWithEndingPar( String rtfControlWord, RtfPara para )
  {
    try
    {
      StringBuilder out = new StringBuilder( 1024 );
      out.append( "{\\"  );
      out.append( rtfControlWord  );
      para.rtf( out, true ) ;
      out.append( '}' );
      return out;
    }
    catch ( IOException e )
    {
      throw new RtfException( e );
    }
  }

  /**
   * Escape character with <code>\'xx</code> type escaping using windows-1252 encoding.
   */
  static String escapeWindows1252(char c) {
    if(!charsetEncoder.canEncode(c)) {
      return "?";
    }

    try
    {
      final ByteBuffer bytes = charsetEncoder.encode(CharBuffer.wrap(String.valueOf(c)));
      final int unsignedCharByte = bytes.get() & 255; // Treat byte as unsigned
      return String.format("\\'%02x", unsignedCharByte);
    }
    catch (CharacterCodingException err)
    {
      throw new RtfException( err );
    }
  }
}
