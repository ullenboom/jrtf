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
 * Represents a RTF paragraph.
 */
public abstract class RtfTextPara extends RtfPara
{
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
  
  /** Paragraph formattings. */
  private StringBuilder parfmt = new StringBuilder( 512 );

  /** Tabulator definitions. */
  private StringBuilder tabdef = new StringBuilder( 512 );
  
  /** Border definitions. */
  private StringBuilder brdrdef = new StringBuilder( 512 );

  /** Cell formattings. Not private so it can be accessed by RtfPara (bad design anyway). */
  StringBuilder cellfmt = new StringBuilder( 32 );

  /**
   * Returns the RTF control words for the <textpar> formattings.
   * @return
   */
  CharSequence textparFormatRtf()
  {
    return new StringBuilder( 512 )
                 .append( brdrdef ).append( parfmt ).append( tabdef );
  }

  // Paragraph-Formatting Properties
  // parfmt
  
  /**
   * Resets to default paragraph properties.
   * @return {@code this}-object.
   */
  public RtfTextPara reset()
  {
    parfmt.append( "\\pard\n" );
    return this;
  }

//  /**
//   * Style of this paragraph.
//   * @param stylenumber Style number as defined in header.
//   * @return {@code this}-object.
//   */
//  public RtfTextPara style( int stylenumber )
//  {
//    parfmt.append( "\\s" ).append( stylenumber ).append( '\n' );
//    return this;
//  }
  
  /**
   * Hyphenation for the paragraph on.
   * @return {@code this}-object.
   */
  public RtfTextPara hyphenationOn()
  {
    parfmt.append( "\\hyphpar1\n" );
    return this;
  }

  /**
   * Hyphenation for the paragraph off.
   * @return {@code this}-object.
   */
  public RtfTextPara hyphenationOff()
  {
    parfmt.append( "\\hyphpar0\n" );
    return this;
  }
  
  /**
   * Paragraph is part of a table.
   * @return {@code this}-object.
   */
  public RtfTextPara partOfTable()
  {
    parfmt.append( "\\intbl\n" );
    return this;
  }

  /**
   * Keep paragraph intact.
   * @return {@code this}-object.
   */
  public RtfTextPara keep()
  {
    parfmt.append( "\\keep\n" );
    return this;
  }

  /**
   * No widow/orphan control.
   * @return {@code this}-object.
   */
  public RtfTextPara noWidowOrOrphanControl()
  {
    parfmt.append( "\\nowidctlpar\n" );
    return this;
  }

  /**
   * Keep paragraph with the next paragraph.
   * @return {@code this}-object.
   */
  public RtfTextPara keepWithNextParagraph()
  {
    parfmt.append( "\\keepn\n" );
    return this;
  }

  /**
   * Outline level of the paragraph.
   * @param level Outline level.
   * @return {@code this}-object.
   */
  public RtfTextPara level( int level )
  {
    if ( level < 0 )
      throw new IllegalArgumentException( "Level is not allowed to be negative but is " + level );

    parfmt.append( "\\level" ).append( level ).append( '\n' );
    return this;
  }

  /**
   * No line numbering.
   * @return {@code this}-object.
   */
  public RtfTextPara noLineNumbering()
  {
    parfmt.append( "\\noline\n" );
    return this;
  }

  /**
   * Break page before the paragraph.
   * @return {@code this}-object.
   */
  public RtfTextPara breakPageBeforeParagraph()
  {
    parfmt.append( "\\pagebb\n" );
    return this;
  }

  //
  // Alignment
  //

  /**
   * @return {@code this}-object.
   */
  public RtfTextPara alignLeft()
  {
    parfmt.append( "\\ql\n" );
    return this;
  }

  /**
   * @return {@code this}-object.
   */
  public RtfTextPara alignRight()
  {
    parfmt.append( "\\qr\n" );
    return this;
  }

  /**
   * 
   * @return {@code this}-object.
   */
  public RtfTextPara alignJustified()
  {
    parfmt.append( "\\qj\n" );
    return this;
  }

  /**
   * 
   * @return {@code this}-object.
   */
  public RtfTextPara alignCentered()
  {
    parfmt.append( "\\qc\n" );
    return this;
  }
  
  //
  //
  //

  /**
   * First-line indent by given amount.
   * @param indentation Indentation.
   * @param unit Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara indentFirstLine( double indentation, RtfUnit unit )
  {
    parfmt.append( "\\fi" ).append( unit.toTwips( indentation ) ).append( '\n' );
    return this;
  }

  /**
   * Left indent by given amount.
   * @param indentation Indentation.
   * @param unit Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara indentLeft( double indentation, RtfUnit unit )
  {
    parfmt.append( "\\li" ).append( unit.toTwips( indentation ) ).append( '\n' );
    return this;
  }

  /**
   * Right indent by given amount.
   * @param indentation Indentation.
   * @param unit Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara indentRight( double indentation, RtfUnit unit )
  {
    parfmt.append( "\\ri" ).append( unit.toTwips( indentation ) ).append( '\n' );
    return this;
  }
  
  //
  // Spacing
  //
  
  /**
   * Space before line by given amount. If not set default is 0.
   * @param space Space.
   * @param unit Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara spaceBeforeLine( double space, RtfUnit unit )
  {
    parfmt.append( "\\sb" ).append( unit.toTwips( space ) ).append( '\n' );
    return this;
  }
  
  /**
   * Space after line by given amount. If not set default is 0.
   * @param space Space.
   * @param unit Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara spaceAfterLine( double space, RtfUnit unit )
  {
    parfmt.append( "\\sa" ).append( unit.toTwips( space ) ).append( '\n' );
    return this;
  }

  /**
   * Space between lines by a given amount. If not set default is 0.
   * @param space Space. If {@code space} is a positive value, this size is
   *                     used only if it's taller than the tallest character
   *                     (otherwise, the tallest character is used).
   *                     If {@code space} is a negative value, the absolute
   *                     value of {@code space} is used, even if it is shorter
   *                     than the tallest character.
   * @param unit Measurement unit.
   * @return {@code this}-object.
   */
  public RtfTextPara spaceBetweenLines( double space, RtfUnit unit )
  {
    space = Math.abs( space );
    
    parfmt.append( "\\sl" ).append( unit.toTwips( space ) ).append( '\n' );
    return this;
  }

  /**
   * "At Least" or "exactly" space between lines is a multiple of single line spacing.
   * If not set default is 0.
   * @param space Single space between lines.
   * @param unit Measurement unit.
   * @return {@code this}-object.
   * @see #spaceBetweenLines(double, RtfUnit)
   */
  public RtfTextPara spaceBetweenLinesMultipleAtLeastOrExactly( double space, RtfUnit unit )
  {
    space = Math.abs( space );

    parfmt.append( "\\sl" ).append( unit.toTwips( space ) ).append( "\\slmult0\n" );
    return this;
  }

  /**
   * Space between lines is a multiple of single line spacing.
   * If not set default is 0.
   * @param space Space.
   * @param unit Measurement unit.
   * @return {@code this}-object.
   * @see #spaceBetweenLines(double, RtfUnit)
   */
  public RtfTextPara spaceBetweenLinesMultiple( double space, RtfUnit unit )
  {
    space = Math.abs( space );

    parfmt.append( "\\sl" ).append( unit.toTwips( space ) ).append( "\\slmult1\n" );
    return this;
  }

  /**
   * Line spacing is automatically determined by the tallest character in the line.
   * The call is equal to {@link #spaceBetweenLines(double, RtfUnit)} with 0 as
   * first argument.
   * @return {@code this}-object.
   * @see #spaceBetweenLines(double, RtfUnit)
   */
  public RtfTextPara spaceBetweenLinesAutomatically()
  {
    parfmt.append( "\\sl0\n" );
    return this;
  }

  
  //
  // Bidirectional controls
  //
  
  /**
   * Text in this paragraph will be displayed with right to left precedence.
   * @return {@code this}-object.
   */
  public RtfTextPara rightToLeft()
  {
    parfmt.append( "\\rtlpar\n" );
    return this;
  }
  
  /**
   * Text in this paragraph will be displayed with left to right precedence. This is the default.
   * @return {@code this}-object.
   */
  public RtfTextPara leftToRight()
  {
    parfmt.append( "\\ltrpar\n" );
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
  public enum TabKind
  {
    /** Left tab. Default. */
    LEFT,

    /** Flush-right tab. Align text right. */
    RIGHT { @Override public String toString() { return "\\tqr"; } },

    /** Centered tab. Align text centered. */
    CENTER { @Override public String toString() { return "\\tqc"; } },

    /** Decimal tab. Align text on the decimal character. */
    DECIMAL { @Override public String toString() { return "\\tqdec"; } }
  }

  /**
   * Leading symbols.
   */
  public enum TabLead
  {
    /** Leader dots. */
    DOTS { @Override public String toString() { return "\\tldot"; } },

    /** Leader hyphens. */
    HYPHENS { @Override public String toString() { return "\\tlhyph"; } },
    
    /** Leader underline. */
    UNDERLINE { @Override public String toString() { return "\\tlul"; } },

    /** Leader thick line.. */
    THICK_LINE { @Override public String toString() { return "\\tlth"; } },
    
    /** Leader equal sign. */
    EQUALS_SIGN { @Override public String toString() { return "\\tleq"; } }
  }

  /**
   * Defines a tab.
   * @param tabPostion  Position of the tabulator.
   * @param unit Measurement.
   * @return {@code this}-object.
   */
  public RtfTextPara tab( double tabPostion, RtfUnit unit )
  {
    return tab( null, null, tabPostion, unit );
  }

  /**
   * Defines a tab.
   * @param tabKind What kind of tab. Can be {@code null}.
   * @param tabPostion Position of the tabulator.
   * @param unit Measurement.
   * @return {@code this}-object.
   */
  public RtfTextPara tab( TabKind tabKind, double tabPostion, RtfUnit unit )
  {
    return tab( tabKind, null, tabPostion, unit );
  }  

  /**
   * Defines a tab with an additional tab lead. 
   * @param tabKind What kind of tab. Can be {@code null}.
   * @param tabLead Leading characters. Can be {@code null}.
   * @param tabPostion Position of the tabulator.
   * @param unit Measurement.
   * @return {@code this}-object.
   */
  public RtfTextPara tab( TabKind tabKind, TabLead tabLead, double tabPostion, RtfUnit unit )
  {
    if ( tabKind != null && tabKind != TabKind.LEFT )
      tabdef.append( tabKind );

    if ( tabLead != null )
      tabdef.append( tabLead );

    tabdef.append( "\\tx" ).append( unit.toTwips( tabPostion ) ).append( '\n' );
    return this;
  }  

//  /**
//   * @param tabPostion Position of the tabulator.
//   * @param unit Measurement.
//   * @return {@code this}-object.
//   */
//  public RtfTextPara bartab( double tabPostion, RtfUnit unit )
//  {
//    return bartab( null, tabPostion, unit );
//  }
//
//  /**
//   * @param tabLead Leading characters. Can be {@code null}.
//   * @param tabPostion Position of the tabulator.
//   * @param unit Measurement.
//   * @return {@code this}-object.
//   */
//  public RtfTextPara bartab( TabLead tabLead, double tabPostion, RtfUnit unit )
//  {
//    if ( tabLead != null )
//      tabdef.append( tabLead );
//
//    tabdef.append( "\\tb" ).append( unit.toTwips( tabPostion ) ).append( '\n' );
//    return this;
//  }  

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
  public enum BorderStyle
  {
    /** Single-thickness border. */
    SINGLE  {@Override public String toString() { return "\\brdrs"; } },

    /** Double-thickness border. */
    DOUBLE_THICKNESS {@Override public String toString() { return "\\\brdrth"; } },

    /** Shadowed border. */
    SHADOWED {@Override public String toString() { return "\\\brdrsh"; } },

    /** Double border. */
    DOUBLE {@Override public String toString() { return "\\\brdrdb"; } },

    /** Dotted border. */
    DOTTED {@Override public String toString() { return "\\\brdrdot"; } },
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
   * @param borderStyle Style of the border.
   * @return {@code this}-object.
   */
  public RtfTextPara topBorder( BorderStyle borderStyle )
  {
    if ( borderStyle == null )
      throw new IllegalArgumentException( "Border style is missing, can't be null" );

    brdrdef.append( "\\brdrt" ).append( borderStyle );
    return this;
  }
  
  /**
   * Border bottom.
   * @param borderStyle Style of the border.
   * @return {@code this}-object.
   */
  public RtfTextPara bottomBorder( BorderStyle borderStyle )
  {
    if ( borderStyle == null )
      throw new IllegalArgumentException( "Border style is missing, can't be null" );

    brdrdef.append( "\\brdrb" ).append( borderStyle );
    return this;
  }
  /**
   * Border left.
   * @param borderStyle Style of the border.
   * @return {@code this}-object.
   */
  public RtfTextPara leftBorder( BorderStyle borderStyle )
  {
    if ( borderStyle == null )
      throw new IllegalArgumentException( "Border style is missing, can't be null" );

    brdrdef.append( "\\brdrl" ).append( borderStyle );
    return this;
  }

  /**
   * Border right.
   * @param borderStyle Style of the border.
   * @return {@code this}-object.
   */
  public RtfTextPara rightBorder( BorderStyle borderStyle )
  {
    if ( borderStyle == null )
      throw new IllegalArgumentException( "Border style is missing, can't be null" );

    brdrdef.append( "\\brdrr" ).append( borderStyle );
    return this;
  }
  
  /**
   * Sets the width of a cell if the paragraph is used in a table.
   * @param width  Width of the cell.
   * @param unit   Unit of the width.
   * @return {@code this}-object.
   */
  public RtfTextPara cellWidth( double width, RtfUnit unit )
  {
    cellfmt.append( "\\clftsWidth3\\clwWidth" )
          .append( unit.toTwips( Math.abs( width ) ) )
          .append( '\n' );

    return this;
  }
}
