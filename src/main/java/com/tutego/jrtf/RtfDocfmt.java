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
 * Instances represent document format like page width, margins.
 */
public class RtfDocfmt {

  /* <document> := <info>? <docfmt>* <section>+ */

  private final Consumer<RtfOutput> renderer;

  private RtfDocfmt( Consumer<RtfOutput> renderer ) {
    this.renderer = renderer;
  }

  void rtf( RtfOutput out ) {
    renderer.accept( out );
  }

  // Paper format constants

  public static final RtfDocfmt A0 = paper( 84.1, 118.9, RtfUnit.CM );
  public static final RtfDocfmt A1 = paper( 59.4, 84.1, RtfUnit.CM );
  public static final RtfDocfmt A2 = paper( 42.0, 59.4, RtfUnit.CM );
  public static final RtfDocfmt A3 = paper( 29.7, 42.0, RtfUnit.CM );
  public static final RtfDocfmt A4 = paper( 21.0, 29.7, RtfUnit.CM );
  public static final RtfDocfmt A5 = paper( 14.8, 21.0, RtfUnit.CM );
  public static final RtfDocfmt A6 = paper( 10.5, 14.8, RtfUnit.CM );
  public static final RtfDocfmt A7 = paper( 7.4, 10.5, RtfUnit.CM );
  public static final RtfDocfmt A8 = paper( 5.2, 7.4, RtfUnit.CM );

  // General control words

  public static RtfDocfmt defaultTab( double width, RtfUnit unit ) {
    int twips = unit.toTwips( width );
    return new RtfDocfmt( out -> out.cw( RtfControlWords.DEFAULT_TAB_WIDTH, twips ) );
  }

  public static RtfDocfmt hyphenationHotZone( double width, RtfUnit unit ) {
    int twips = unit.toTwips( width );
    return new RtfDocfmt( out -> out.cw( RtfControlWords.HYPHENATION_HOT_ZONE, twips ) );
  }

  // Document Views

  public static RtfDocfmt pageLayoutView() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.VIEW_KIND ).append( "1" ) );
  }

  // Footnotes and Endnotes

  public static RtfDocfmt footnotesOnly() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.FOOTNOTE_ENDNOTE_PLACEMENT ).append( "0" ) );
  }

  public static RtfDocfmt endnotesOnly() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.FOOTNOTE_ENDNOTE_PLACEMENT ).append( "1" ) );
  }

  public static RtfDocfmt footnotesEndnotes() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.FOOTNOTE_ENDNOTE_PLACEMENT ).append( "2" ) );
  }

  public static RtfDocfmt footnoteNumberingArabic() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.FOOTNOTE_NUMBERING_ARABIC ) );
  }

  public static RtfDocfmt footnoteNumberingUpperAlphabetic() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.FOOTNOTE_NUMBERING_UPPER_ALPHA ) );
  }

  public static RtfDocfmt footnoteNumberingUpperRoman() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.FOOTNOTE_NUMBERING_UPPER_ROMAN ) );
  }

  // Page information

  public static RtfDocfmt paperWidth( double width, RtfUnit unit ) {
    int twips = unit.toTwips( width );
    return new RtfDocfmt( out -> out.cw( RtfControlWords.PAPER_WIDTH, twips ) );
  }

  public static RtfDocfmt paperHeight( double height, RtfUnit unit ) {
    int twips = unit.toTwips( height );
    return new RtfDocfmt( out -> out.cw( RtfControlWords.PAPER_HEIGHT, twips ) );
  }

  public static RtfDocfmt paper( double width, double height, RtfUnit unit ) {
    int w = unit.toTwips( width );
    int h = unit.toTwips( height );
    return new RtfDocfmt( out -> out.cw( RtfControlWords.PAPER_WIDTH, w )
                                    .cw( RtfControlWords.PAPER_HEIGHT, h ) );
  }

  public static RtfDocfmt leftMargin( double margin, RtfUnit unit ) {
    int twips = unit.toTwips( margin );
    return new RtfDocfmt( out -> out.cw( RtfControlWords.MARGIN_LEFT, twips ) );
  }

  public static RtfDocfmt rightMargin( double margin, RtfUnit unit ) {
    int twips = unit.toTwips( margin );
    return new RtfDocfmt( out -> out.cw( RtfControlWords.MARGIN_RIGHT, twips ) );
  }

  public static RtfDocfmt topMargin( double margin, RtfUnit unit ) {
    int twips = unit.toTwips( margin );
    return new RtfDocfmt( out -> out.cw( RtfControlWords.MARGIN_TOP, twips ) );
  }

  public static RtfDocfmt bottomMargin( double margin, RtfUnit unit ) {
    int twips = unit.toTwips( margin );
    return new RtfDocfmt( out -> out.cw( RtfControlWords.MARGIN_BOTTOM, twips ) );
  }

  public static RtfDocfmt facingPages() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.FACING_PAGES ) );
  }

  public static RtfDocfmt switchMargin() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.MIRROR_MARGINS ) );
  }

  public static RtfDocfmt landscape() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.LANDSCAPE ) );
  }

  public static RtfDocfmt widowOrphanControl() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.WIDOW_CONTROL ) );
  }

  // Revision marks

  public static RtfDocfmt revisionProtected() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.REVISION_PROTECTED ) );
  }

  public static RtfDocfmt revisionMarking() {
    return new RtfDocfmt( out -> out.cw( RtfControlWords.REVISION_MARKING ) );
  }
}
