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
 * Instances represent document format like page width, margins.
 */
public class RtfDocfmt
{
    /** A0 paper format. */
  public static final RtfDocfmt A0 = paper( 118.9, 84.1, RtfUnit.CM );

  /** A1 paper format. */
  public static final RtfDocfmt A1 = paper( 84.1, 59.4, RtfUnit.CM );
  
  /** A2 paper format. */
  public static final RtfDocfmt A2 = paper( 59.4, 42.0, RtfUnit.CM );
  
  /** A3 paper format. */
  public static final RtfDocfmt A3 = paper( 42.0, 21.0, RtfUnit.CM );
  
  /** A4 paper format. */
  public static final RtfDocfmt A4 = paper( 29.7, 21.0, RtfUnit.CM );
  
  /** A5 paper format. */
  public static final RtfDocfmt A5 = paper( 21.0, 14.8, RtfUnit.CM );
  
  /** A6 paper format. */
  public static final RtfDocfmt A6 = paper( 14.8, 10.5, RtfUnit.CM );
  
  /** A7 paper format. */
  public static final RtfDocfmt A7 = paper( 10.5, 7.4, RtfUnit.CM );
  
  /** A8 paper format. */
  public static final RtfDocfmt A8 = paper( 7.4, 5.2, RtfUnit.CM );

  /* <document> := <info>? <docfmt>* <section>+ */                 

  /** RTF with format string. */
  String rtf;

  /**
   * Initialize this instance with the RTF control word.
   * @param rtf
   */
  private RtfDocfmt( String rtf )
  {
    this.rtf = rtf;
  }

  // General control words
  
  /**
   * Default tab width. If not set the default is 720 twips.
   * @param width  Width of the tab.
   * @param unit   Unit of the tab.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt defaultTab( double width, RtfUnit unit )
  {
    return new RtfDocfmt( "\\deftab" + unit.toTwips( width ) );
  }

  /**
   * Hyphenation hot zone.
   * @param width   Amount of space at the right margin in which words
   *                are hyphenated.
   * @param unit    Unit of the tab.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt hyphenationHotZone( double width, RtfUnit unit )
  {
    return new RtfDocfmt( "\\hyphhotzN" + unit.toTwips( width ) );
  }

  // Document Views and Zoom Level

  /**
   * Sets the view mode of the document to page layout.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt pageLayoutView()
  {
    return new RtfDocfmt( "\\viewkind1" );
  }
  
  // Footnotes and Endnotes

  /**
   * Footnotes only or nothing at all (default).
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt footnotesOnly()
  {
    return new RtfDocfmt( "\\fet0" );
  }

  /**
   * Endnotes only.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt endnotesOnly()
  {
    return new RtfDocfmt( "\\fet1" );
  }

  /**
   * Footnotes and endnotes both.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt footnotesEndnotes()
  {
    return new RtfDocfmt( "\\fet2" );
  }

  /**
   * Footnote numbering - Arabic numbering (1, 2, 3, _).
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt footnoteNumberingArabic()
  {
    return new RtfDocfmt( "\\ftnnar" );
  }

  /**
   * Footnote numbering - Alphabetic uppercase (A, B, C, _).
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt footnoteNumberingUpperAlphabetic()
  {
    return new RtfDocfmt( "\\ftnnauc" );
  }

  /**
   * Footnote numbering - Roman lowercase (i, ii, iii, _).
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt footnoteNumberingUpperRoman()
  {
    return new RtfDocfmt( "\\ftnnrlc" );
  }

  // Page information

  /**
   * Paper width. Default is 12,240 twips.
   * @param width  Width of the page.
   * @param unit   Measurement unit.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt paperWidth( double width, RtfUnit unit)
  {
    return new RtfDocfmt( "\\paperw" + unit.toTwips( width ) );
  }

  /**
   * Paper height. Default is 15,840 twips.
   * @param height Height of the page.
   * @param unit   Measurement unit.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt paperHeight( double height, RtfUnit unit)
  {
    return new RtfDocfmt( "\\paperh" + unit.toTwips( height ) );
  }

  /**
   * Paper width and height.
   * @param width  Height of the page.
   * @param height Height of the page.
   * @param unit   Measurement unit.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt paper( double width, double height, RtfUnit unit)
  {
    return new RtfDocfmt( "\\paperw" + unit.toTwips( width ) +
                          "\\paperh" + unit.toTwips( height) );
  }
  
  /**
   * Left margin. Default is 1,800 twips.
   * @param margin Left margin.
   * @param unit Measurement unit.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt leftMargin( double margin, RtfUnit unit)
  {
    return new RtfDocfmt( "\\margl" + unit.toTwips( margin ) );
  }

  /**
   * Right margin. Default is 1,800 twips.
   * @param margin Right margin.
   * @param unit Measurement unit.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt rightMargin( double margin, RtfUnit unit)
  {
    return new RtfDocfmt( "\\margr" + unit.toTwips( margin ) );
  }

  /**
   * Top margin. Default is 1,440 twips.
   * @param margin Top margin.
   * @param unit Measurement unit.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt topMargin( double margin, RtfUnit unit)
  {
    return new RtfDocfmt( "\\margt" + unit.toTwips( margin ) );
  }

  /**
   * Bottom margin. Default is 1,440 twips.
   * @param margin Bottom margin.
   * @param unit Measurement unit.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt bottomMargin( double margin, RtfUnit unit)
  {
    return new RtfDocfmt( "\\margb" + unit.toTwips( margin ) );
  }

  /**
   * Facing pages (activates odd/even headers and gutters).
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt facingPages()
  {
    return new RtfDocfmt( "\\facingp" );
  }

  /**
   * Switches margin definitions on left and right pages.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt switchMargin()
  {
    return new RtfDocfmt( "\\margmirror" );
  }

  /**
   * Landscape format.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt landscape()
  {
    return new RtfDocfmt( "\\landscape" );
  }

  /**
   * Enable widow and orphan control.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt widowOrphanControl()
  {
    return new RtfDocfmt( "\\widowctrl" );
  }
  
  // Revision marks

  /**
   * This document is protected for revisions. The user can edit the document, but revision marking cannot be
   * disabled.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt revisionProtected()
  {
    return new RtfDocfmt( "\\revprot" );
  }

  /**
   * Turns on revision marking.
   * @return New {@code RtfDocfmt} object.
   */
  public static RtfDocfmt revisionMarking()
  {
    return new RtfDocfmt( "\\revisions" );
  }
}
