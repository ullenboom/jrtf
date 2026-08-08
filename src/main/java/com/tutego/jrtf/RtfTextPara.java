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

import org.jspecify.annotations.Nullable;

/**
 * Represents a RTF paragraph.
 */
public class RtfTextPara extends RtfPara {

  Consumer<RtfOutput> renderer;
  int styleId;
  boolean resetDefaults;
  boolean emptyParagraph;

  /** Package-private: called from {@link RtfPara#p} / {@link RtfPara#pard}. */
  RtfTextPara() {}

  @Override void rtf( RtfOutput out, boolean withEndingPar ) {
    if ( emptyParagraph ) {
      if ( resetDefaults )
        out.cw( RtfControlWords.PARAGRAPH_DEFAULTS );
      if ( withEndingPar )
        out.cw( RtfControlWords.PAR );
      return;
    }
    out.open();
    if ( resetDefaults )
      out.cw( RtfControlWords.PARAGRAPH_DEFAULTS );
    out.cw( RtfControlWords.STYLE ).append( styleId ).sp();
    out.append( textparFormatRtf() );
    if ( renderer != null )
      renderer.accept( out );
    if ( withEndingPar )
      out.cw( RtfControlWords.PAR );
    out.close().nl();
  }
  /*
   * <textpar> := <pn>?
   *              <brdrdef>?
   *              <parfmt>*
   *              <apoctl>*
   *              <tabdef>?
   *              <shading>?
   *              (\subdocument | <char>+)
   *              (\par <para>)?
   */

  /**
   * Paragraph formattings.
   */
  private final StringBuilder parfmt = new StringBuilder( 512 );

  /**
   * Tabulator definitions.
   */
  private final StringBuilder tabdef = new StringBuilder( 512 );

  /**
   * Border definitions.
   */
  private final StringBuilder brdrdef = new StringBuilder( 512 );

  /**
   * Cell formattings. Not private so it can be accessed by RtfPara (bad design anyway).
   */
  final StringBuilder cellfmt = new StringBuilder( 32 );

  /**
   * Explicit cell width in twips when this paragraph is used as a table cell, or {@code -1}
   * if not set. Kept numerically (in addition to {@link #cellfmt}) so a row can compute the
   * cumulative {@code \cellx} right boundaries. Read by {@link RtfPara#rowWithBackgroundColor}.
   */
  int cellWidthTwips = -1;

  /**
   * Returns the RTF control words for the <textpar> formattings.
   *
   * @return
   */
  CharSequence textparFormatRtf() {
    return new StringBuilder( 512 )
        .append( brdrdef ).append( parfmt ).append( tabdef );
  }

  // Paragraph-Formatting Properties
  // parfmt

  /**
   * Resets to default paragraph properties.
   *
   * @return {@code this}-object.
   */
  public RtfTextPara reset() {
    parfmt.append( '\\' ).append( RtfControlWords.PARAGRAPH_DEFAULTS ).append( '\n' );
    return this;
  }

  /**
   * Hyphenation for the paragraph on.
   *
   * @return {@code this}-object.
   */
  public RtfTextPara hyphenationOn() {
    parfmt.append( '\\' ).append( RtfControlWords.HYPHENATE_PARAGRAPH ).append( "1\n" );
    return this;
  }

  /**
   * Hyphenation for the paragraph off.
   *
   * @return {@code this}-object.
   */
  public RtfTextPara hyphenationOff() {
    parfmt.append( '\\' ).append( RtfControlWords.HYPHENATE_PARAGRAPH ).append( "0\n" );
    return this;
  }

  /**
   * Paragraph is part of a table.
   *
   * @return {@code this}-object.
   */
  public RtfTextPara partOfTable() {
    parfmt.append( '\\' ).append( RtfControlWords.IN_TABLE ).append( '\n' );
    return this;
  }

  /**
   * Keep paragraph intact.
   *
   * @return {@code this}-object.
   */
  public RtfTextPara keep() {
    parfmt.append( '\\' ).append( RtfControlWords.KEEP_TOGETHER ).append( '\n' );
    return this;
  }

  /**
   * No widow/orphan control.
   *
   * @return {@code this}-object.
   */
  public RtfTextPara noWidowOrOrphanControl() {
    parfmt.append( '\\' ).append( RtfControlWords.NO_WIDOW_CONTROL ).append( '\n' );
    return this;
  }

  /**
   * Keep paragraph with the next paragraph.
   *
   * @return {@code this}-object.
   */
  public RtfTextPara keepWithNextParagraph() {
    parfmt.append( '\\' ).append( RtfControlWords.KEEP_WITH_NEXT ).append( '\n' );
    return this;
  }

  /**
   * Outline level of the paragraph.
   *
   * @param level Outline level.
   * @return {@code this}-object.
   */
  public RtfTextPara level( int level ) {
    if ( level < 0 )
      throw new IllegalArgumentException( "Level is not allowed to be negative but is " + level );

    parfmt.append( '\\' ).append( RtfControlWords.OUTLINE_LEVEL ).append( level ).append( '\n' );
    return this;
  }

  // Shading patterns

  /** Patterns for paragraph shading beyond solid fill. */
  public enum ShadingPattern {
    HORIZ( RtfControlWords.SHADING_PATTERN_HORIZ ),
    VERT( RtfControlWords.SHADING_PATTERN_VERT ),
    FORWARD_DIAG( RtfControlWords.SHADING_PATTERN_FDIAG ),
    BACKWARD_DIAG( RtfControlWords.SHADING_PATTERN_BDIAG ),
    CROSS( RtfControlWords.SHADING_PATTERN_CROSS ),
    DIAGONAL_CROSS( RtfControlWords.SHADING_PATTERN_DCROSS );

    final String controlWord;
    ShadingPattern( String cw ) { this.controlWord = cw; }
  }

  /**
   * Sets the shading pattern for the paragraph background.
   * Also applies to table cells when used on a cell's paragraph.
   *
   * @param pattern      Shading pattern.
   * @param colorIndex   Background color index in the color table.
   * @return {@code this}-object.
   */
  public RtfTextPara shadingPattern( ShadingPattern pattern, int colorIndex ) {
    parfmt.append( '\\' ).append( pattern.controlWord )
          .append( '\\' ).append( RtfControlWords.PARAGRAPH_BACKGROUND_COLOR )
          .append( colorIndex ).append( '\n' );
    return this;
  }

  /**
   * No line numbering.
   *
   * @return {@code this}-object.
   */
  public RtfTextPara noLineNumbering() {
    parfmt.append( '\\' ).append( RtfControlWords.NO_LINE_NUMBERING ).append( '\n' );
    return this;
  }

  /**
   * Sets the language for the entire paragraph using a {@link java.util.Locale},
   * which is mapped to the closest LCID. Prefer this over the numeric version.
   *
   * @param locale Language to use for spell checking and hyphenation.
   * @return {@code this}-object.
   */
  public RtfTextPara language( java.util.Locale locale ) {
    // Lookup via RtfText's table; defer to the numeric overload
    return language( lcidFromLocale( locale ) );
  }

  /**
   * Sets the language for the entire paragraph, used for spell checking
   * and hyphenation. Common LCIDs: 1031 (German), 1033 (US English).
   * Prefer {@link #language(java.util.Locale)} when possible.
   *
   * @param lcid Windows Language Code Identifier.
   * @return {@code this}-object.
   */
  public RtfTextPara language( int lcid ) {
    parfmt.append( '\\' ).append( RtfControlWords.LANGUAGE ).append( lcid ).append( '\n' );
    return this;
  }

  private static int lcidFromLocale( java.util.Locale locale ) {
    Integer lcid = RtfText.LOCALE_TO_LCID.get( locale );
    if ( lcid == null )
      throw new IllegalArgumentException(
          "No LCID mapping for " + locale + ". Use language(int lcid) with "
          + "the numeric Windows Language Code Identifier." );
    return lcid;
  }

  /**
   * Break page before the paragraph.
   *
   * @return {@code this}-object.
   */
  public RtfTextPara breakPageBeforeParagraph() {
    parfmt.append( '\\' ).append( RtfControlWords.PAGE_BREAK_BEFORE ).append( '\n' );
    return this;
  }

  //
  // Alignment
  //

  /**
   * @return {@code this}-object.
   */
  public RtfTextPara alignLeft() {
    parfmt.append( '\\' ).append( RtfControlWords.ALIGN_LEFT ).append( '\n' );
    return this;
  }

  /**
   * @return {@code this}-object.
   */
  public RtfTextPara alignRight() {
    parfmt.append( '\\' ).append( RtfControlWords.ALIGN_RIGHT ).append( '\n' );
    return this;
  }

  /**
   *
   * @return {@code this}-object.
   */
  public RtfTextPara alignJustified() {
    parfmt.append( '\\' ).append( RtfControlWords.ALIGN_JUSTIFIED ).append( '\n' );
    return this;
  }

  /**
   *
   * @return {@code this}-object.
   */
  public RtfTextPara alignCentered() {
    parfmt.append( '\\' ).append( RtfControlWords.ALIGN_CENTERED ).append( '\n' );
    return this;
  }

  //
  //
  //

  /**
   * First-line indent by given amount.
   *
   * @param indentation Indentation.
   * @param unit        Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara indentFirstLine( double indentation, RtfUnit unit ) {
    parfmt.append( '\\' ).append( RtfControlWords.FIRST_LINE_INDENT ).append( unit.toTwips( indentation ) ).append( '\n' );
    return this;
  }

  /**
   * Left indent by given amount.
   *
   * @param indentation Indentation.
   * @param unit        Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara indentLeft( double indentation, RtfUnit unit ) {
    parfmt.append( '\\' ).append( RtfControlWords.LEFT_INDENT ).append( unit.toTwips( indentation ) ).append( '\n' );
    return this;
  }

  /**
   * Right indent by given amount.
   *
   * @param indentation Indentation.
   * @param unit        Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara indentRight( double indentation, RtfUnit unit ) {
    parfmt.append( '\\' ).append( RtfControlWords.RIGHT_INDENT ).append( unit.toTwips( indentation ) ).append( '\n' );
    return this;
  }

  //
  // Spacing
  //

  /**
   * Space before line by given amount. If not set default is 0.
   *
   * @param space Space.
   * @param unit  Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara spaceBeforeLine( double space, RtfUnit unit ) {
    parfmt.append( '\\' ).append( RtfControlWords.SPACE_BEFORE ).append( unit.toTwips( space ) ).append( '\n' );
    return this;
  }

  /**
   * Space after line by given amount. If not set default is 0.
   *
   * @param space Space.
   * @param unit  Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara spaceAfterLine( double space, RtfUnit unit ) {
    parfmt.append( '\\' ).append( RtfControlWords.SPACE_AFTER ).append( unit.toTwips( space ) ).append( '\n' );
    return this;
  }

  /**
   * Space between lines by a given amount. If not set default is 0.
   *
   * @param space Space. If {@code space} is a positive value, this size is
   *              used only if it's taller than the tallest character
   *              (otherwise, the tallest character is used).
   *              If {@code space} is a negative value, the absolute
   *              value of {@code space} is used, even if it is shorter
   *              than the tallest character.
   * @param unit  Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara spaceBetweenLines( double space, RtfUnit unit ) {
    // Sign matters: positive = at least, negative = exactly (RTF spec)
    parfmt.append( '\\' ).append( RtfControlWords.LINE_SPACING ).append( unit.toTwips( space ) ).append( '\n' );
    return this;
  }

  /**
   * "At Least" or "exactly" space between lines is a multiple of single line spacing.
   * If not set default is 0.
   *
   * @param space Single space between lines.
   * @param unit  Measurement unit.
   * @return {@code this}-object.
   * @see #spaceBetweenLines(double, RtfUnit)
   */
  public RtfTextPara spaceBetweenLinesMultipleAtLeastOrExactly( double space, RtfUnit unit ) {
    space = Math.abs( space );

    parfmt.append( '\\' ).append( RtfControlWords.LINE_SPACING ).append( unit.toTwips( space ) )
          .append( '\\' ).append( RtfControlWords.LINE_SPACING_MULTIPLE ).append( "0\n" );
    return this;
  }

  /**
   * Space between lines is a multiple of single line spacing.
   * If not set default is 0.
   *
   * @param space Space.
   * @param unit  Measurement unit.
   * @return {@code this}-object.
   * @see #spaceBetweenLines(double, RtfUnit)
   */
  public RtfTextPara spaceBetweenLinesMultiple( double space, RtfUnit unit ) {
    space = Math.abs( space );

    parfmt.append( '\\' ).append( RtfControlWords.LINE_SPACING ).append( unit.toTwips( space ) )
          .append( '\\' ).append( RtfControlWords.LINE_SPACING_MULTIPLE ).append( "1\n" );
    return this;
  }

  /**
   * Line spacing is automatically determined by the tallest character in the line.
   * The call is equal to {@link #spaceBetweenLines(double, RtfUnit)} with 0 as
   * first argument.
   *
   * @return {@code this}-object.
   * @see #spaceBetweenLines(double, RtfUnit)
   */
  public RtfTextPara spaceBetweenLinesAutomatically() {
    parfmt.append( '\\' ).append( RtfControlWords.LINE_SPACING ).append( "0\n" );
    return this;
  }

  //
  // Bidirectional controls
  //

  /**
   * Text in this paragraph will be displayed with right to left precedence.
   *
   * @return {@code this}-object.
   */
  public RtfTextPara rightToLeft() {
    parfmt.append( '\\' ).append( RtfControlWords.RIGHT_TO_LEFT_PARAGRAPH ).append( '\n' );
    return this;
  }

  /**
   * Text in this paragraph will be displayed with left to right precedence. This is the default.
   *
   * @return {@code this}-object.
   */
  public RtfTextPara leftToRight() {
    parfmt.append( '\\' ).append( RtfControlWords.LEFT_TO_RIGHT_PARAGRAPH ).append( '\n' );
    return this;
  }

  // Tabs

  /*
   * <tabdef>   :=  (<tab> | <bartab>) +
   * <tab>      :=  <tabkind>? <tablead>? \tx
   * <bartab>   :=  <tablead>? \tb
   * <tabkind>  :=  \tqr | \ tqc | \ tqdec
   * <tablead>  :=  \tldot | \tlhyph | \tlul | \tleq
   */

  /**
   * Different kind of tabs.
   */
  public enum TabKind {
    /**
     * Left tab. Default.
     */
    LEFT,

    /**
     * Flush-right tab. Align text right.
     */
    RIGHT {
      @Override public String toString() {return RtfControlWords.TAB_RIGHT;}
    },

    /**
     * Centered tab. Align text centered.
     */
    CENTER {
      @Override public String toString() {return RtfControlWords.TAB_CENTER;}
    },

    /**
     * Decimal tab. Align text on the decimal character.
     */
    DECIMAL {
      @Override public String toString() {return RtfControlWords.TAB_DECIMAL;}
    },

    /**
     * Hanging tab for lead indentation.
     */
    HANGING
  }

  /**
   * Leading symbols.
   */
  public enum TabLead {
    /**
     * Leader dots.
     */
    DOTS {
      @Override public String toString() {return RtfControlWords.TAB_LEAD_DOTS;}
    },

    /**
     * Leader hyphens.
     */
    HYPHENS {
      @Override public String toString() {return RtfControlWords.TAB_LEAD_HYPHENS;}
    },

    /**
     * Leader underline.
     */
    UNDERLINE {
      @Override public String toString() {return RtfControlWords.TAB_LEAD_UNDERLINE;}
    },

    /**
     * Leader thick line..
     */
    THICK_LINE {
      @Override public String toString() {return RtfControlWords.TAB_LEAD_THICK_LINE;}
    },

    /**
     * Leader equal sign.
     */
    EQUALS_SIGN {
      @Override public String toString() {return RtfControlWords.TAB_LEAD_EQUALS_SIGN;}
    }
  }

  /**
   * Defines a tab.
   *
   * @param tabPostion Position of the tabulator.
   * @param unit       Measurement.
   * @return {@code this}-object.
   */
  public RtfTextPara tab( double tabPostion, RtfUnit unit ) {
    return tab( null, null, tabPostion, unit );
  }

  /**
   * Defines a tab.
   *
   * @param tabKind    What kind of tab. May be {@code null} (defaults to a left tab).
   * @param tabPostion Position of the tabulator.
   * @param unit       Measurement.
   * @return {@code this}-object.
   */
  public RtfTextPara tab( @Nullable TabKind tabKind, double tabPostion, RtfUnit unit ) {
    return tab( tabKind, null, tabPostion, unit );
  }

  /**
   * Defines a tab with an additional tab lead.
   *
   * @param tabKind    What kind of tab. May be {@code null} (defaults to a left tab).
   * @param tabLead    Leading characters. May be {@code null} (no lead).
   * @param tabPostion Position of the tabulator.
   * @param unit       Measurement.
   * @return {@code this}-object.
   */
  public RtfTextPara tab( @Nullable TabKind tabKind, @Nullable TabLead tabLead, double tabPostion, RtfUnit unit ) {
    if ( tabKind != null && tabKind != TabKind.LEFT && tabKind != TabKind.HANGING )
      tabdef.append( '\\' ).append( tabKind );

    if ( tabLead != null )
      tabdef.append( '\\' ).append( tabLead );

    if ( tabKind == TabKind.HANGING ) {
      int twips = unit.toTwips( tabPostion );
      tabdef.append( String.format( "\\" + RtfControlWords.LEFT_INDENT + "%d" + "\\" + RtfControlWords.FIRST_LINE_INDENT + "-%d",
          twips, twips ) ).append( '\n' );
    }
    else {
      tabdef.append( '\\' ).append( RtfControlWords.TAB_POSITION ).append( unit.toTwips( tabPostion ) ).append( '\n' );
    }

    return this;
  }

  /**
   * Defines a bar tab (a vertical bar drawn at the tab position).
   *
   * @param tabPostion Position of the tabulator.
   * @param unit       Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara bartab( double tabPostion, RtfUnit unit ) {
    return bartab( null, tabPostion, unit );
  }

  /**
   * Defines a bar tab (a vertical bar drawn at the tab position) with an additional tab lead.
   *
   * @param tabLead    Leading characters. May be {@code null} (no lead).
   * @param tabPostion Position of the tabulator.
   * @param unit       Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara bartab( @Nullable TabLead tabLead, double tabPostion, RtfUnit unit ) {
    if ( tabLead != null )
      tabdef.append( '\\' ).append( tabLead );

    tabdef.append( '\\' ).append( RtfControlWords.BAR_TAB_POSITION ).append( unit.toTwips( tabPostion ) ).append( '\n' );
    return this;
  }

  // Borders

  // Paragraph Borders

  /*
   * <brdrdef>  := (<brdrseg> <brdr> )+
   * <brdrseg>  := \brdrt | \brdrb | \brdrl | \brdrr | \brdrbtw | \brdrbar | \box
   * <brdr>     := <brdrk> \brdrw? \brsp? \brdrcf?
   * <brdrk>    := \brdrs | \brdrth | \brdrsh | \brdrdb | \brdrdot | \brdrdash |
   *               \brdrhair | brdrinset | \brdrdashsm | \brdrdashd | \brdrdashdd |
   *               \brdrtriple | \brdrtnthsg | \brdrthtnsg | \brdrtnthtnsg |
   *               \brdrtnthmg | \brdrthtnmg | \brdrtnthtnmg | \brdrtnthlg |
   *               \brdrthtnlg | \brdrtnthtnlg | \brdrwavy | \brdrwavydb |
   *               \brdrdashdotstr | \brdremboss | \brdrengrave \brdroutset |
   *               \brdrnone | \brdrtbl | \brdrnil
   */

  /**
   * Enumerator for different border styles.
   */
  public enum BorderStyle {
    /**
     * Single-thickness border.
     */
    SINGLE {
      @Override public String toString() {return RtfControlWords.BORDER_SINGLE;}
    },

    /**
     * Double-thickness border.
     */
    DOUBLE_THICKNESS {
      @Override public String toString() {return RtfControlWords.BORDER_DOUBLE_THICKNESS;}
    },

    /**
     * Shadowed border.
     */
    SHADOWED {
      @Override public String toString() {return RtfControlWords.BORDER_SHADOWED;}
    },

    /**
     * Double border.
     */
    DOUBLE {
      @Override public String toString() {return RtfControlWords.BORDER_DOUBLE;}
    },

    /**
     * Dotted border.
     */
    DOTTED {
      @Override public String toString() {return RtfControlWords.BORDER_DOTTED;}
    },

    /**
     * Dashed border.
     */
    DASHED {
      @Override public String toString() {return RtfControlWords.BORDER_DASHED;}
    },

    /**
     * Hairline border.
     */
    HAIRLINE {
      @Override public String toString() {return RtfControlWords.BORDER_HAIRLINE;}
    },
  }
  
  /*
  \brdrdash Dashed border.
  \brdrhair Hairline border.
  \brdrinset  Inset border.
  \brdrdashsm Dashed border (small).
  \brdrdashd  Dot-dashed border.
  \brdrdashdd Dot-dot-dashed border.
  \brdroutset Outset border.
  \brdrtriple Triple border.
  \brdrtnthsg Thick-thin border (small).
  \brdrthtnsg Thin-thick border (small).
  \brdrtnthtnsg Thin-thick thin border (small).
  \brdrtnthmg Thick-thin border (medium).
  \brdrthtnmg Thin-thick border (medium).
  \brdrtnthtnmg Thin-thick thin border (medium).
  \brdrtnthlg Thick-thin border (large).
  \brdrthtnlg Thin-thick border (large).
  \brdrtnthtnlg Thin-thick-thin border (large).
  \brdrwavy Wavy border.
  \brdrwavydb Double wavy border.
  \brdrdashdotstr Striped border.
  \brdremboss Embossed border.
  \brdrengrave  Engraved border.
  \brdrframe  Border resembles a "Frame."
  \brdrwN N is the width in twips of the pen used to draw the paragraph border line. N cannot be greater than 75. To obtain a larger border width, the \brdth control word can be used to obtain a width double that of N.
  \brdrcfN  N is the color of the paragraph border, specified as an index into the color table in the RTF header. 
  \brspN  Space in twips between borders and the paragraph.
  \brdrnil  No border specified.
   */

  /**
   * Border top.
   *
   * @param borderStyle Style of the border.
   * @return {@code this}-object.
   */
  public RtfTextPara topBorder( BorderStyle borderStyle ) {
    return topBorder( borderStyle, -1, RtfUnit.TWIPS, -1 );
  }

  /**
   * Border top with an explicit width and color.
   *
   * @param borderStyle Style of the border.
   * @param width       Width of the border line. Ignored (no {@code \brdrw} written) if negative.
   * @param unit        Measurement unit of {@code width}.
   * @param colorIndex  Index into the header color table. Ignored (no {@code \brdrcf} written) if negative.
   * @return {@code this}-object.
   */
  public RtfTextPara topBorder( BorderStyle borderStyle, double width, RtfUnit unit, int colorIndex ) {
    if ( borderStyle == null )
      throw new IllegalArgumentException( "Border style is missing, can't be null" );

    brdrdef.append( '\\' ).append( RtfControlWords.PARAGRAPH_BORDER_TOP ).append( '\\' ).append( borderStyle );
    appendBorderWidthAndColor( width, unit, colorIndex );
    return this;
  }

  /**
   * Border bottom.
   *
   * @param borderStyle Style of the border.
   * @return {@code this}-object.
   */
  public RtfTextPara bottomBorder( BorderStyle borderStyle ) {
    return bottomBorder( borderStyle, -1, RtfUnit.TWIPS, -1 );
  }

  /**
   * Border bottom with an explicit width and color.
   *
   * @param borderStyle Style of the border.
   * @param width       Width of the border line. Ignored (no {@code \brdrw} written) if negative.
   * @param unit        Measurement unit of {@code width}.
   * @param colorIndex  Index into the header color table. Ignored (no {@code \brdrcf} written) if negative.
   * @return {@code this}-object.
   */
  public RtfTextPara bottomBorder( BorderStyle borderStyle, double width, RtfUnit unit, int colorIndex ) {
    if ( borderStyle == null )
      throw new IllegalArgumentException( "Border style is missing, can't be null" );

    brdrdef.append( '\\' ).append( RtfControlWords.PARAGRAPH_BORDER_BOTTOM ).append( '\\' ).append( borderStyle );
    appendBorderWidthAndColor( width, unit, colorIndex );
    return this;
  }

  /**
   * Border left.
   *
   * @param borderStyle Style of the border.
   * @return {@code this}-object.
   */
  public RtfTextPara leftBorder( BorderStyle borderStyle ) {
    return leftBorder( borderStyle, -1, RtfUnit.TWIPS, -1 );
  }

  /**
   * Border left with an explicit width and color.
   *
   * @param borderStyle Style of the border.
   * @param width       Width of the border line. Ignored (no {@code \brdrw} written) if negative.
   * @param unit        Measurement unit of {@code width}.
   * @param colorIndex  Index into the header color table. Ignored (no {@code \brdrcf} written) if negative.
   * @return {@code this}-object.
   */
  public RtfTextPara leftBorder( BorderStyle borderStyle, double width, RtfUnit unit, int colorIndex ) {
    if ( borderStyle == null )
      throw new IllegalArgumentException( "Border style is missing, can't be null" );

    brdrdef.append( '\\' ).append( RtfControlWords.PARAGRAPH_BORDER_LEFT ).append( '\\' ).append( borderStyle );
    appendBorderWidthAndColor( width, unit, colorIndex );
    return this;
  }

  /**
   * Border right.
   *
   * @param borderStyle Style of the border.
   * @return {@code this}-object.
   */
  public RtfTextPara rightBorder( BorderStyle borderStyle ) {
    return rightBorder( borderStyle, -1, RtfUnit.TWIPS, -1 );
  }

  /**
   * Border right with an explicit width and color.
   *
   * @param borderStyle Style of the border.
   * @param width       Width of the border line. Ignored (no {@code \brdrw} written) if negative.
   * @param unit        Measurement unit of {@code width}.
   * @param colorIndex  Index into the header color table. Ignored (no {@code \brdrcf} written) if negative.
   * @return {@code this}-object.
   */
  public RtfTextPara rightBorder( BorderStyle borderStyle, double width, RtfUnit unit, int colorIndex ) {
    if ( borderStyle == null )
      throw new IllegalArgumentException( "Border style is missing, can't be null" );

    brdrdef.append( '\\' ).append( RtfControlWords.PARAGRAPH_BORDER_RIGHT ).append( '\\' ).append( borderStyle );
    appendBorderWidthAndColor( width, unit, colorIndex );
    return this;
  }

  private void appendBorderWidthAndColor( double width, RtfUnit unit, int colorIndex ) {
    if ( width >= 0 )
      brdrdef.append( '\\' ).append( RtfControlWords.BORDER_WIDTH ).append( unit.toTwips( width ) );
    if ( colorIndex >= 0 )
      brdrdef.append( '\\' ).append( RtfControlWords.BORDER_COLOR ).append( colorIndex );
  }

  /**
   * Sets the background (shading) color of this paragraph.
   *
   * @param colorIndex Index of the color as defined in the header color table.
   * @return {@code this}-object.
   */
  public RtfTextPara backgroundColor( int colorIndex ) {
    parfmt.append( '\\' ).append( RtfControlWords.PARAGRAPH_SHADING ).append( "10000" )
          .append( '\\' ).append( RtfControlWords.PARAGRAPH_BACKGROUND_COLOR ).append( colorIndex ).append( '\n' );
    return this;
  }

  /**
   * Marks this paragraph as an item of the given {@link RtfList} at the given level, so
   * word processors render and interactively renumber a genuine bullet or number &mdash;
   * unlike {@link RtfPara#ul(RtfText)} / {@link RtfPara#ol}, whose marker is literal text.
   * Also sets the paragraph's left/first-line indent to match the level, for readers that
   * don't support {@code \listtable}.
   *
   * @param list       List this paragraph belongs to. Must already be registered with
   *                   {@link Rtf#lists(RtfList...)}.
   * @param levelIndex 0-based level of {@code list} this paragraph uses.
   * @return {@code this}-object.
   */
  public RtfTextPara list( RtfList list, int levelIndex ) {
    if ( list == null )
      throw new IllegalArgumentException( "List can't be null" );
    if ( list.overrideIndex < 0 )
      throw new RtfException( "List must be registered with Rtf.lists(...) before it can be used" );
    if ( levelIndex < 0 || levelIndex >= list.levelCount() )
      throw new IllegalArgumentException( "Level " + levelIndex + " is not configured on this list" );

    parfmt.append( '\\' ).append( RtfControlWords.LEFT_INDENT ).append( list.indentTwipsAt( levelIndex ) )
          .append( '\\' ).append( RtfControlWords.FIRST_LINE_INDENT ).append( "-" ).append( list.hangingTwipsAt( levelIndex ) )
          .append( '\\' ).append( RtfControlWords.LIST_OVERRIDE_INDEX ).append( list.overrideIndex )
          .append( '\\' ).append( RtfControlWords.LIST_LEVEL_INDEX ).append( levelIndex )
          .append( '\n' );

    return this;
  }

  /**
   * Sets the width of a cell if the paragraph is used in a table.
   *
   * @param width Width of the cell.
   * @param unit  Unit of the width.
   * @return {@code this}-object.
   */
  public RtfTextPara cellWidth( double width, RtfUnit unit ) {
    this.cellWidthTwips = unit.toTwips( Math.abs( width ) );
    cellfmt.append( '\\' ).append( RtfControlWords.CELL_WIDTH_TYPE_FIXED ).append( '\\' ).append( RtfControlWords.CELL_WIDTH )
           .append( cellWidthTwips )
           .append( '\n' );

    return this;
  }
}
