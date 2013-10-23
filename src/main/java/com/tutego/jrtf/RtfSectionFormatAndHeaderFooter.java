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

/**
 * Section-Formatting.
 */
public class RtfSectionFormatAndHeaderFooter
{
  /** Holds the RTF text for a header or footer element. */
  final CharSequence rtf;

  /**
   * Initializes this object with RTF text.
   * @param rtf  RTF.
   */
  private RtfSectionFormatAndHeaderFooter( CharSequence rtf )
  {
    this.rtf = rtf;
  }

  /*
   * <section>  := <secfmt>* <hdrftr>? <para>+ (\sect <section>)?
   */

  /**
   * Builds a RtfSectionFormatAndHeaderFooter objects representing all the
   * section formattings.
   * @param sectionFormats  Sequence of section formats.
   * @return RtfSectionFormatAndHeaderFooter
   */
  public static RtfSectionFormatAndHeaderFooter sectionFormatting( RtfSectionFormatAndHeaderFooter... sectionFormats )
  {
    StringBuilder sb = new StringBuilder( sectionFormats.length * 10 );
    for ( RtfSectionFormatAndHeaderFooter rtfSectionFormat : sectionFormats )
      sb.append( rtfSectionFormat.rtf );

    return new RtfSectionFormatAndHeaderFooter( sb );
  }

  /**
   * Reset to default section properties of this current section.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter reset()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\sectd" );
  }

  /**
   * Endnotes are included in this section.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter endnotesIncluded()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\endnhere" );
  }

  // Section break
  
  /**
   * No section break.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter noBreak()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\sbknone" );
  }
  
  /**
   * Section break starts a new column.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter breakStartsNewColumn()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\sbkcol" );
  }

  /**
   * Section break starts a new page. That is the default.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter breakStartsNewPage()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\sbkpage" );
  }

  /**
   * Section break starts at an even page.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter breakStartsNewEvenPage()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\sbkeven" );
  }

  /**
   * Section break starts at an odd page.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter breakStartsNewOddPage()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\sbkodd" );
  }

  // Columns

  /**
   * Number of columns in this current section. Default is 1.
   * @param columns Number of columns for this section.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter columns( int columns )
  {
    if ( columns <= 0 )
      throw new RtfException( "Number of colums can't be <= 0" );

    return new RtfSectionFormatAndHeaderFooter( "\\cols" + columns );
  }

  /**
   * Space between columns in this current section. Default is 720 twips.
   * @param space Space between columns.
   * @param unit Measurement.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter spaceBetweenColumns( double space, RtfUnit unit )
  {
    return new RtfSectionFormatAndHeaderFooter( "\\colsx" + unit.toTwips( space ));
  }

  /**
   * Puts a line between columns in this current section.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter lineBetweenColumns()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\linebetcol" );
  }
  
  // Line numbering

  // Page information
  
  // Page numbers

  /**
   * Beginning page number of this current section. Default is 1.
   * @param pageNumber Beginning page number.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter beginningPageNumber( int pageNumber )
  {
    return new RtfSectionFormatAndHeaderFooter( "\\pgnstarts" + pageNumber );
  }

  // Vertical alignment

  /**
   * Text is top-aligned. This is the default.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter topAlignText()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\vertalt" );
  }

  /**
   * Text is bottom-aligned.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter bottomAlignText()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\vertalb" );
  }

  /**
   * Text is centered vertically.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter centerVerticalText()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\vertalc" );
  }

  /**
   * Text is justified vertically.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter justifyVerticalText()
  {
    return new RtfSectionFormatAndHeaderFooter( "\\vertalj" );
  }


  /*
   * <section> := <secfmt>* <hdrftr>? <para>+ ( \sect <section>)?
   * <hdrftr>  := '{' <hdrctl> <para>+ '}' <hdrftr>?
   * <hdrctl>  := \header | \footer | \headerl | \headerr | \headerf | \footerl | \ footerr | \footerf
   */
 
  /**
   * Puts a header with a given paragraph on all pages in this current section.
   * @param para Paragraph for the header.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter headerForAllPages( RtfPara para )
  {
    return new RtfSectionFormatAndHeaderFooter( Rtf.frameRtfParagraphWithEndingPar( "header", para ) );
  }

  /**
   * Puts a header with a given paragraph on all left-hand pages in this current section.
   * @param para Paragraph for the header.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter headerForLeftHandPages( RtfPara para )
  {
    return new RtfSectionFormatAndHeaderFooter( Rtf.frameRtfParagraphWithEndingPar( "headerl", para ) );
  }

  /**
   * Puts a header with a given paragraph on all right-hand pages in this current section.
   * @param para Paragraph for the header.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter headerForRightHandPages( RtfPara para )
  {
    return new RtfSectionFormatAndHeaderFooter( Rtf.frameRtfParagraphWithEndingPar( "headerr", para ) );
  }

  /**
   * Puts a header with a given paragraph on the first page in this current section.
   * @param para Paragraph for the header.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter headerForFirstPage( RtfPara para )
  {
    return new RtfSectionFormatAndHeaderFooter( Rtf.frameRtfParagraphWithEndingPar( "headerf", para ) );
  }

  /**
   * Puts a footer with a given paragraph on all pages in this current section.
   * @param para Paragraph for the footer.
   * @return New {@code RtfSectionFormatAndHeaderFooter} object.
   */
  public static RtfSectionFormatAndHeaderFooter footerOnAllPages( RtfPara para )
  {
    return new RtfSectionFormatAndHeaderFooter( Rtf.frameRtfParagraphWithEndingPar( "footer", para ) );
  }
}
