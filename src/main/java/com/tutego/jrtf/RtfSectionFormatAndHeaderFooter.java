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

import java.util.function.Consumer;

/**
 * Section-Formatting.
 */
public class RtfSectionFormatAndHeaderFooter {

  private final Consumer<RtfOutput> renderer;

  private RtfSectionFormatAndHeaderFooter( Consumer<RtfOutput> renderer ) {
    this.renderer = renderer;
  }

  void rtf( RtfOutput out ) {
    renderer.accept( out );
  }

  /*
   * <section>  := <secfmt>* <hdrftr>? <para>+ (\sect <section>)?
   */

  public static RtfSectionFormatAndHeaderFooter sectionFormatting(
      RtfSectionFormatAndHeaderFooter... sectionFormats ) {
    return new RtfSectionFormatAndHeaderFooter( out -> {
      for ( RtfSectionFormatAndHeaderFooter fmt : sectionFormats )
        fmt.rtf( out );
    } );
  }

  public static RtfSectionFormatAndHeaderFooter reset() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.SECTION_DEFAULTS ) );
  }

  public static RtfSectionFormatAndHeaderFooter endnotesIncluded() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.END_NOTES_HERE ) );
  }

  // Section break

  public static RtfSectionFormatAndHeaderFooter noBreak() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.SECTION_BREAK_NONE ) );
  }

  public static RtfSectionFormatAndHeaderFooter breakStartsNewColumn() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.SECTION_BREAK_COLUMN ) );
  }

  public static RtfSectionFormatAndHeaderFooter breakStartsNewPage() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.SECTION_BREAK_PAGE ) );
  }

  public static RtfSectionFormatAndHeaderFooter breakStartsNewEvenPage() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.SECTION_BREAK_EVEN ) );
  }

  public static RtfSectionFormatAndHeaderFooter breakStartsNewOddPage() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.SECTION_BREAK_ODD ) );
  }

  // Columns

  public static RtfSectionFormatAndHeaderFooter columns( int columns ) {
    if ( columns <= 0 )
      throw new RtfException( "Number of colums can't be <= 0" );
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.COLUMNS, columns ) );
  }

  public static RtfSectionFormatAndHeaderFooter spaceBetweenColumns( double space, RtfUnit unit ) {
    int twips = unit.toTwips( space );
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.COLUMN_SPACE, twips ) );
  }

  public static RtfSectionFormatAndHeaderFooter lineBetweenColumns() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.LINE_BETWEEN_COLUMNS ) );
  }

  // Page numbers

  public static RtfSectionFormatAndHeaderFooter beginningPageNumber( int pageNumber ) {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.PAGE_NUMBER_RESTART, pageNumber ) );
  }

  public static RtfSectionFormatAndHeaderFooter pageNumberLowerRoman() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.PAGE_NUMBER_LOWER_ROMAN ) );
  }

  public static RtfSectionFormatAndHeaderFooter pageNumberUpperRoman() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.PAGE_NUMBER_UPPER_ROMAN ) );
  }

  public static RtfSectionFormatAndHeaderFooter pageNumberDecimal() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.PAGE_NUMBER_DECIMAL ) );
  }

  public static RtfSectionFormatAndHeaderFooter titlePage() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.TITLE_PAGE ) );
  }

  // Vertical alignment

  public static RtfSectionFormatAndHeaderFooter topAlignText() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.VERTICAL_ALIGN_TOP ) );
  }

  public static RtfSectionFormatAndHeaderFooter bottomAlignText() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.VERTICAL_ALIGN_BOTTOM ) );
  }

  public static RtfSectionFormatAndHeaderFooter centerVerticalText() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.VERTICAL_ALIGN_CENTER ) );
  }

  public static RtfSectionFormatAndHeaderFooter justifyVerticalText() {
    return new RtfSectionFormatAndHeaderFooter( out -> out.cw( RtfControlWords.VERTICAL_ALIGN_JUSTIFIED ) );
  }

  // Headers and footers

  /*
   * <section> := <secfmt>* <hdrftr>? <para>+ ( \sect <section>)?
   * <hdrftr>  := '{' <hdrctl> <para>+ '}' <hdrftr>?
   * <hdrctl>  := \header | \footer | \headerl | \headerr | \headerf | \footerl | \ footerr | \footerf
   */

  public static RtfSectionFormatAndHeaderFooter headerForAllPages( RtfPara para ) {
    return headerFooter( "header", para );
  }

  public static RtfSectionFormatAndHeaderFooter headerForLeftHandPages( RtfPara para ) {
    return headerFooter( "headerl", para );
  }

  public static RtfSectionFormatAndHeaderFooter headerForRightHandPages( RtfPara para ) {
    return headerFooter( "headerr", para );
  }

  public static RtfSectionFormatAndHeaderFooter headerForFirstPage( RtfPara para ) {
    return headerFooter( "headerf", para );
  }

  public static RtfSectionFormatAndHeaderFooter footerOnAllPages( RtfPara para ) {
    return headerFooter( "footer", para );
  }

  private static RtfSectionFormatAndHeaderFooter headerFooter( String controlWord, RtfPara para ) {
    return new RtfSectionFormatAndHeaderFooter( out -> {
      out.open( controlWord );
      para.rtf( out, true );
      out.close();
    } );
  }
}
