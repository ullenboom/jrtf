/*
 * Copyright (c) 2010-2014 Christian Ullenboom 
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
import java.net.URL;

/**
 * Class for RTF text with different text formattings like bold, italic, ...
 */
public class RtfText
{
  /*
   * <para>    := <textpar> | <row>
   * <textpar> := <pn>? <brdrdef>? <parfmt>* <apoctl>* <tabdef>? <shading>?
   *              (/v /spv)? (\subdocument | <char>+) (\par <para>)?
   * 
   * <char>    := <ptext> | <atext> | '{' <char> '}'
   * <ptext>   := (<chrfmt>* <data>+ )+
   * <data>    := #PCDATA | <spec> | <pict> | <obj> |
   *              <do> | <foot> | <annot> | <field> |
   *              <idx> | <toc> | <book>
   */
  
  /** RTF text. */
  private final CharSequence rtf;

  /** For wrapping RTF text to this object. */
  RtfText( CharSequence rtf )
  {
    this.rtf = rtf;
  }

  /**
   * Writes the RTF of this RtfText object to the output.
   * @param out Appendable.
   * @throws IOException
   */
  void rtf( Appendable out ) throws IOException
  {
    out.append( rtf );
  }

  /**
   * Converts every object in the sequence with {@code toString} to a
   * String object and wraps it into a RtfText if its not already a
   * RtfText object.
   * If the argument is {@code null} or no elements are given
   * the result is equal to {@code text("")}. If one element of the vararg
   * is {@code null} then this element is omitted and no separating space
   * will be written too.
   * @param texts Sequence of text.
   * @return New RtfText object representing this sequence of text.
   */
  public static RtfText text( Object... texts )
  {
    return textJoinWithSpace( false, texts );
  }

  /**
   * Converts every object in the sequence with {@code toString} to a
   * String object and wraps it into a RtfText if its not already a
   * RtfText object and join every text with a space between if wanted.
   * If the argument is {@code null} or no elements are given
   * the result is equal to {@code text("")}. If one element of the vararg
   * is {@code null} then this element is omitted and no separating space
   * will be written too.
   * @param joinWithSpace If space character should be set between non {@code null} elements.
   * @param texts Sequence of text.
   * @return New RtfText object representing this sequence of text.
   */
  public static RtfText textJoinWithSpace( boolean joinWithSpace, final Object... texts )
  {
    if ( texts == null || texts.length == 0 )
      return new RtfText( "" );

    StringBuilder result = new StringBuilder( 1024 );
    for ( int i = 0; i < texts.length; i++ )
    {
      if ( texts[ i ] == null )
        continue;

      if ( joinWithSpace )
        if ( i > 0 && texts[ i - 1 ] != null )  // if preceding element is null, no space
          result.append( ' ' );

      try
      {
        if ( texts[ i ] instanceof RtfText )
          ((RtfText) texts[ i ]).rtf( result );
        else if ( texts[ i ] instanceof RtfPara )  // check more
          throw new RtfException( "RtfPara in method text() is not allowed. There is no sensible toString() method declared" );
        else
          Rtf.asRtf( result, texts[ i ].toString() );
      }
      catch ( IOException e ) { throw new RtfException( e ); }
    }
    return new RtfText( result );
  }

  /**
   * Wraps a String in a {@link RtfText} object. 
   * If the argument is a {@code null} value then it will be treated like a {@code ""}.
   * @param text String
   * @return New RtfText object representing this text.
   */
  public static RtfText text( String text )
  {
    if ( text == null )
      text = "";

    return new RtfText( Rtf.asRtf( text ) );
  }  
  
  /**
   * Sets text with a given font style.
   * @param fontnum Font number according to the header.
   * @param text Text to set with a different font.
   * @return New RtfText object representing this text.
   */
  public static RtfText font( int fontnum, Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 8 );
    sb.append( "{\\f" ).append( fontnum )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );

    return new RtfText( sb );
  }

  /**
   * Italic given text.
   * @param text Text to make italic.
   * @return New RtfText object representing this text.
   */
  public static RtfText italic( Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 5 );
    sb.append( "{\\i " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Bold a given text.
   * @param text Text to be set bold.
   * @return New RtfText object representing this bold text.
   */
  public static RtfText bold( Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 5 );
    sb.append( "{\\b " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Underline text.
   * @param text Text to underline.
   * @return New RtfText object representing this underlined text.
   */
  public static RtfText underline( Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 6 );
    sb.append( "{\\ul " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Underlines dotted.
   * @param text Text to underline dotted.
   * @return New RtfText object representing this text.
   */
  public static RtfText dottedUnderline( String text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 7 );
    sb.append( "{\\uld " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Underlines double.
   * @param text Text to underline double.
   * @return New RtfText object representing this text.
   */
  public static RtfText doubleUnderline( Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 8 );
    sb.append( "{\\uldb " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Underlines word.
   * @param text Text where words will be underlined.
   * @return New RtfText object representing this text.
   */
  public static RtfText wordUnderline( Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 7 );
    sb.append( "{\\ulw " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Subscripts text.
   * @param text Text to subscript.
   * @return New RtfText object representing this subscripted text.
   */
  public static RtfText subscript( Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 7 );
    sb.append( "{\\sub " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Set text for revision.
   * @param text Text for revision.
   * @return New RtfText object representing this resivisioned text.
   */
  public static RtfText revised( Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 11 );
    sb.append( "{\\revised " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Superscripts text.
   * @param text Text to superscript.
   * @return New RtfText object representing this superscripted text.
   */
  public static RtfText superscript( Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 9 );
    sb.append( "{\\super " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Strikes through text.
   * @param text Text to strike through.
   * @return New RtfText object representing this strikes through text.
   */
  public static RtfText strikethru( Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 10 );
    sb.append( "{\\strike " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Shadows text.
   * @param text Text to shadow.
   * @return New RtfText object representing this shadowed text.
   */
  public static RtfText shadow( Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 8 );
    sb.append( "{\\shad " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Shows text in small capitals.
   * @param text Text to show in small capitals.
   * @return New RtfText object representing this text.
   */
  public static RtfText smallCapitals( Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 9 );
    sb.append( "{\\scaps " ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Sets the font size in half-points. The default is 24.
   * @param fontSize Font size.
   * @param text Text to set in a different font size.
   * @return New RtfText object representing this text.
   */
  public static RtfText fontSize( int fontSize, Object text )
  {
    if ( fontSize < 0 )
      throw new IllegalArgumentException( "Font size can't be negative" );

    RtfText rtfText = text( text );

    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 10 );
    sb.append( "{\\fs" ).append( fontSize )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );

    return new RtfText( sb );
  }

  //public static RtfText backgroundcolor( int colorindex, RtfText element )
  //{
  //  return new RtfText( "\\cb" + colorindex + " " + element.rtf() + "" );
  //}
  
  //public static RtfText backgroundcolor( int colorindex, String text )
  //{
  //  return backgroundcolor( colorindex, text( text ) );
  //}

  /**
   * Colors text.
   * @param colorindex Index of the color set defined in the header.
   * @param text Text to color.
   * @return New RtfText object representing this text.
   */
  public static RtfText color( int colorindex, Object text )
  {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 10 );
    sb.append( "{\\cf" ).append( colorindex )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );

    return new RtfText( sb );
  }

  /**
   * Colors text.
   * @param colorindex Index of the color set defined in the header.
   * @param text Text to color.
   * @return New RtfText object representing this text.
   */
  public static RtfText color( int colorindex, String text )
  {
    return color( colorindex, text( text ) );
  }
  
  //public static RtfText color( int colorindex1, int colorindex2, RtfText element )
  //{
  //  return new RtfText( "\\cf" + colorindex1 + "\\cb" + colorindex2 + " " + element.rtf() + "" );
  //}
  //
  //public static RtfText color( int colorindex1, int colorindex2, String text )
  //{
  //  return color( colorindex1, colorindex2, text( text ) );
  //}


  // Special Characters
  // <spec>
  
  /**
   * Current date. Useful in headers.
   * @return New RtfText object representing this current date.
   */
  public static RtfText currentDate()
  {
    return new RtfText( "\\chdate\n" );
  }

  /**
   * Current date in long format. Useful in headers
   * @return New RtfText object representing this current date.
   */
  public static RtfText currentDateLong()
  {
    return new RtfText( "\\chdpl\n" );
  }

  /**
   * Current date in abbreviated format. Useful in headers.
   * @return New RtfText object representing this current date.
   */
  public static RtfText currentDateAbbreviated()
  {
    return new RtfText( "\\chdpa\n" );
  }

  /**
   * Current time. Useful in headers.
   * @return New RtfText object representing current time.
   */
  public static RtfText currentTime()
  {
    return new RtfText( "\\chtime\n" );
  }

  /**
   * Current page number. Useful in headers.
   * @return New RtfText object representing page number.
   */
  public static RtfText currentPageNumber()
  {
    return new RtfText( "\\chpgn\n" );
  }

  /**
   * Current section number. Useful in headers.
   * @return New RtfText object representing the section number.
   */
  public static RtfText currentSectionNumber()
  {
    return new RtfText( "\\sectnum\n" );
  }

  /**
   * Required page break.
   * @return New RtfText object representing a page break.
   */
  public static RtfText pageBreak()
  {
    return new RtfText( "\\page\n" );
  }

  /**
   * Required column break.
   * @return New RtfText object representing a column break.
   */
  public static RtfText columnBreak()
  {
    return new RtfText( "\\column\n" );
  }

  /**
   * Required line break (no paragraph break).
   * @return New RtfText object representing a like break.
   */
  public static RtfText lineBreak()
  {
    return new RtfText( "\\line\n" );
  }

  /**
   * Non-required page break. Emitted as it appears in galley view.
   * @return New RtfText object representing a soft page break.
   */
  public static RtfText softPageBreak()
  {
    return new RtfText( "\\softpage\n" );
  }

  /**
   * Non-required column break. Emitted as it appears in galley view.
   * @return New RtfText object representing a column break.
   */
  public static RtfText softColumnBreak()
  {
    return new RtfText( "\\softcol\n" );
  }

  /**
   * Non-required line break. Emitted as it appears in galley view.
   * @return New RtfText object representing a soft line break.
   */
  public static RtfText softLineBreak()
  {
    return new RtfText( "\\softline\n" );
  }

  /**
   * Tab character. You can also insert a regular "\t" in text.
   * See {@link RtfTextPara#tab(double, RtfUnit)} for adjustments.
   * @return New RtfText object representing a tab.
   */
  public static RtfText tab()
  {
    return new RtfText( "\\tab\n" );
  }

  /**
   * Em-dash (long hyphen).
   * @return New RtfText object representing a hyphen.
   */
  public static RtfText longHyphen()
  {
    return new RtfText( "\\emdash\n" );
  }

  /**
   * En-dash (short hyphen).
   * @return New RtfText object representing a hypen.
   */
  public static RtfText shortHyphen()
  {
    return new RtfText( "\\endash\n" );
  }

  /**
   * Bullet character.
   * @return New RtfText object representing a bullet.
   */
  public static RtfText bullet()
  {
    return new RtfText( "\\bullet\n" );
  }

  /**
   * Left single quotation mark.
   * @return New RtfText object representing this text.
   */
  public static RtfText leftQuotationMark()
  {
    return new RtfText( "\\lquote\n" );
  }

  /**
   * Right single quotation mark.
   * @return New RtfText object representing this text.
   */
  public static RtfText rightQuotationMark()
  {
    return new RtfText( "\\rquote\n" );
  }

  /**
   * Sets a text in single quotation marks.
   * @param text Text to put in quotes.
   * @return New RtfText object representing this text in quotation marks.
   */
  public static RtfText qoute( Object text )
  {
    RtfText rtfText = text( text );
    return new RtfText( "\\lquote\n" + rtfText.rtf + "\\rquote\n" );
  }
  
  /**
   * Left double quotation mark.
   * @return New RtfText object representing this text.
   */
  public static RtfText leftDoubleQuotationMark()
  {
    return new RtfText( "\\ldblquote\n" );
  }

  /**
   * Right double quotation mark.
   * @return New RtfText object representing this text.
   */
  public static RtfText rightDoubleQuotationMark()
  {
    return new RtfText( "\\rdblquote\n" );
  }

  /**
   * Sets a text in double quotation marks.
   * @param text to put in quotes.
   * @return New RtfText object representing this text in quotation marks.
   */
  public static RtfText doubleQuote( Object text )
  {
    RtfText rtfText = text( text );
    return new RtfText( "\\ldblquote\n" + rtfText.rtf + "\\rdblquote\n" );
  }

  /**
   * Non-breaking space.
   * @return New RtfText object representing this text.
   */
  public static RtfText nonBreakingSpace()
  {
    return new RtfText( "\\~\n" );
  }

  // <pict>

  /**
   * Place a picture.
   * @param source  InputStream of the image.
   * @return New {@link RtfPicture} object.
   */
  public static RtfPicture picture( URL source )
  {
    try
    {
      return new RtfPicture( source.openStream() );
    }
    catch ( IOException e )
    {
      throw new RtfException( e );
    }
  }
  
  /**
   * Place a picture.
   * @param source  InputStream of the image.
   * @return New {@link RtfPicture} object.
   */
  public static RtfPicture picture( InputStream source )
  {
    try
    {
      return new RtfPicture( source );
    }
    catch ( IOException e )
    {
      throw new RtfException( e );
    }
  }
  
  // <foot>  '{' \footnote <para>+ '}'

  /**
   * Place a footnote with automatic footnote reference.
   * @param paras Paragraphs of this footnote.
   * @return New RtfText object representing this footnote.
   */
  public static RtfText footnote( final RtfPara... paras )
  {
    return new RtfText( null ) {
      @Override void rtf( Appendable out ) throws IOException
      {
        out.append( "\\chftn{\\footnote{\\up6\\chftn }" );
        for ( RtfPara rtfPara : paras )
          rtfPara.rtf( out, false );
        out.append( "}\n" );        
      }
    };
  }

  /**
   * Place a footnote with automatic footnote reference.
   * @param para Paragraph of this footnote.
   * @return New RtfText object representing this footnote.
   */
  public static RtfText footnote( Object para )
  {
    return footnote( RtfPara.p( para ) );
  }
  
  // Fields  

  /** Modifiers for fields. */
  public enum FieldModifier
  {
    /** A formatting change has been made to the field result since the field was last updated. */
    DIRTY  {@Override public String toString() { return "\\flddirty"; } },
    
    /** Text has been added to, or removed from, the field result since the field was last updated. */
    EDITED  {@Override public String toString() { return "\\fldedit"; } },

    /** Field is locked and cannot be updated. */
    LOCKED  {@Override public String toString() { return "\\fldlock"; } },

    /** Result is not in a form suitable for display (for example, binary data
     *  used by fields whose result is a picture). */
    NONDISPLAYABLE  {@Override public String toString() { return "\\fldpriv"; } }
  }
  
  /**
   * Inserts a RTF field.
   * @param fieldInstructions Field instructions.
   * @param recentResult Recent results of this field.
   * @param fieldModifier Additional field modifier. Can be {@code null}.
   * @return New RtfText object representing this field.
   */
  public static RtfText field( final RtfPara fieldInstructions, final RtfPara recentResult, final FieldModifier fieldModifier )
  {
    if ( fieldInstructions == null )
      throw new IllegalArgumentException( "Field instructions are missing" );
 
    return new RtfText( null ) {
       @Override void rtf(Appendable out) throws IOException
      {
        /*
         * <field>     := '{' \field <fieldmod>? <fieldinst> <fieldrslt> '}'
         * <fieldmod>  := \flddirty? & \fldedit? & \fldlock? & \fldpriv?
         * <fieldinst> := '{\*' \fldinst <para>+ <fldalt>? '}'
         * <fldalt>    := \fldalt
         * <fieldrslt> := '{' \fldrslt <para>+ '}'
         */

        out.append( "{\\field" );

        if ( fieldModifier != null )
          out.append( fieldModifier.toString() );
        
        out.append( "{\\*\\fldinst " );
        fieldInstructions.rtf( out, false );
        out.append( "}{\\fldrslt " );
        
        if ( recentResult != null )
          recentResult.rtf( out, false );

        out.append( "}}" );
      };
    };
  }

  /**
   * Inserts a RTF field.
   * @param fieldInstructions Field instructions.
   * @param recentResult Recent results of this field.
   * @return New RtfText object representing this field.
   */
  public static RtfText field( RtfPara fieldInstructions, RtfPara recentResult )
  {
    return field( fieldInstructions, recentResult, null );
  }
  
  /**
   * Adds an hyperlink (aka anchor). This is a special field.
   * @param url  URL of this hyperlink.
   * @param text Text for this hyperlink.
   * @return New RtfText object representing this hyperlink.
   */
  public static RtfText hyperlink( String url, RtfPara text )
  {
    try
    {
      StringBuilder sb = new StringBuilder( 256 );
      sb.append( "{\\field{\\*\\fldinst{HYPERLINK \"" )
        .append( Rtf.asRtf( url ) )
        .append( "\"}}{\\fldrslt{\\ul " );
      text.rtf( sb, false );
      sb.append( "}}}" );

      return new RtfText( sb );
    }
    catch ( IOException e )
    {
      throw new RtfException( e );
    }
  }
}
