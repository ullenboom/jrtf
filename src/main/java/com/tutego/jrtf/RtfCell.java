/*
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A single cell of a table row, built fluently and specified independently of its neighbours.
 * <p>
 * A cell wraps one or more {@link RtfPara paragraphs} as its content and carries its own
 * cell-definition properties: {@link #width(double, RtfUnit) width}, {@link #backgroundColor(int)
 * background color}, {@link #topBorder() borders}, {@link #verticalAlignCenter() vertical alignment},
 * horizontal/vertical {@link #mergeHorizontally() merging} and the horizontal text alignment of its
 * content. Like everything else in this library, a cell produces no RTF until the enclosing document
 * is written; the fluent methods only accumulate state.
 * <p>
 * Cells are placed into a row with {@link RtfPara#row(RtfCell...)}, which turns the individual widths
 * into the cumulative {@code \cellx} right-boundaries the RTF format requires. Example:
 * <pre>
 * import static com.tutego.jrtf.RtfCell.cell;
 * import static com.tutego.jrtf.RtfText.bold;
 *
 * RtfPara.row(
 *     cell( "Name" ).width( 4, RtfUnit.CM ).backgroundColor( 1 ).allBorders(),
 *     cell( bold( "Value" ) ).width( 6, RtfUnit.CM ).alignRight().verticalAlignCenter().allBorders()
 * );
 * </pre>
 */
public final class RtfCell {
  /**
   * Default cell width in twips (1 inch) used for a cell whose {@link #width(double, RtfUnit)}
   * was not set, so a row can still compute a meaningful cumulative {@code \cellx} boundary.
   */
  static final int DEFAULT_CELL_WIDTH_TWIPS = 1440;

  /**
   * Content paragraphs of this cell. Never empty.
   */
  final List<RtfPara> paras;

  /**
   * Cell-definition control words (borders, shading, vertical alignment, merging, width type),
   * everything except the trailing {@code \cellx} which the enclosing row appends.
   */
  final StringBuilder celldef = new StringBuilder( 32 );

  /**
   * Explicit cell width in twips, or {@code -1} if unset (then {@link #DEFAULT_CELL_WIDTH_TWIPS}
   * is used for the boundary calculation and no explicit {@code \clwWidth} is written).
   */
  int widthTwips = -1;

  private RtfCell( List<RtfPara> paras ) {
    this.paras = paras;
  }

  /**
   * Creates a cell whose content is a single paragraph built from the given objects, exactly like
   * {@link RtfPara#p(Object...)}. Objects that are already {@link RtfText} keep their formatting.
   *
   * @param texts Content of the cell. See {@link RtfText#text(Object...)} for how each element
   *              is resolved (note: unlike {@link RtfPara#row(Object...)}, an {@link RtfPara}
   *              element here is not supported and is rejected, since {@code p(Object...)} always
   *              wraps its arguments into a single new paragraph).
   * @return New cell.
   */
  public static RtfCell cell( Object... texts ) {
    List<RtfPara> paras = new ArrayList<>( 1 );
    paras.add( RtfPara.p( texts ) );
    return new RtfCell( paras );
  }

  /**
   * Creates a cell from one or more ready-made paragraphs. A cell with several paragraphs
   * separates them with {@code \par} and terminates the whole cell with a single {@code \cell}.
   *
   * @param paras Content paragraphs of the cell. Must not be {@code null} or empty.
   * @return New cell.
   */
  public static RtfCell cell( RtfPara... paras ) {
    if ( paras == null || paras.length == 0 )
      throw new RtfException( "A cell needs at least one paragraph" );

    return new RtfCell( new ArrayList<>( Arrays.asList( paras ) ) );
  }

  // Cell size

  /**
   * Sets the width of this cell. When a width is set it is also written as an explicit
   * {@code \clwWidth}; in any case the width feeds the cumulative {@code \cellx} boundary.
   *
   * @param width Width of the cell (its absolute value is used).
   * @param unit  Measurement unit.
   * @return {@code this}-object.
   */
  public RtfCell width( double width, RtfUnit unit ) {
    this.widthTwips = unit.toTwips( Math.abs( width ) );
    celldef.append( RtfControlWords.CELL_WIDTH_TYPE_FIXED )
           .append( RtfControlWords.CELL_WIDTH ).append( widthTwips );
    return this;
  }

  // Shading

  /**
   * Sets the background (shading) color of this cell.
   *
   * @param colorIndex Index of the color as defined in the header color table.
   * @return {@code this}-object.
   */
  public RtfCell backgroundColor( int colorIndex ) {
    celldef.append( RtfControlWords.CELL_BACKGROUND_COLOR ).append( colorIndex );
    return this;
  }

  // Borders

  /**
   * Draws a single-line border at the top of this cell.
   *
   * @return {@code this}-object.
   */
  public RtfCell topBorder() {
    celldef.append( RtfControlWords.CELL_BORDER_TOP ).append( RtfControlWords.BORDER_SINGLE );
    return this;
  }

  /**
   * Draws a single-line border at the bottom of this cell.
   *
   * @return {@code this}-object.
   */
  public RtfCell bottomBorder() {
    celldef.append( RtfControlWords.CELL_BORDER_BOTTOM ).append( RtfControlWords.BORDER_SINGLE );
    return this;
  }

  /**
   * Draws a single-line border on the left of this cell.
   *
   * @return {@code this}-object.
   */
  public RtfCell leftBorder() {
    celldef.append( RtfControlWords.CELL_BORDER_LEFT ).append( RtfControlWords.BORDER_SINGLE );
    return this;
  }

  /**
   * Draws a single-line border on the right of this cell.
   *
   * @return {@code this}-object.
   */
  public RtfCell rightBorder() {
    celldef.append( RtfControlWords.CELL_BORDER_RIGHT ).append( RtfControlWords.BORDER_SINGLE );
    return this;
  }

  /**
   * Draws single-line borders on all four sides of this cell.
   *
   * @return {@code this}-object.
   */
  public RtfCell allBorders() {
    return topBorder().bottomBorder().leftBorder().rightBorder();
  }

  // Vertical alignment of the cell content

  /**
   * Vertically aligns the cell content to the top. This is the default.
   *
   * @return {@code this}-object.
   */
  public RtfCell verticalAlignTop() {
    celldef.append( RtfControlWords.CELL_VERTICAL_ALIGN_TOP );
    return this;
  }

  /**
   * Vertically centers the cell content.
   *
   * @return {@code this}-object.
   */
  public RtfCell verticalAlignCenter() {
    celldef.append( RtfControlWords.CELL_VERTICAL_ALIGN_CENTER );
    return this;
  }

  /**
   * Vertically aligns the cell content to the bottom.
   *
   * @return {@code this}-object.
   */
  public RtfCell verticalAlignBottom() {
    celldef.append( RtfControlWords.CELL_VERTICAL_ALIGN_BOTTOM );
    return this;
  }

  // Merging

  /**
   * Marks this cell as the first one of a horizontally merged range of cells.
   * The following cells of the range use {@link #mergeHorizontally()}.
   *
   * @return {@code this}-object.
   */
  public RtfCell mergeHorizontallyFirst() {
    celldef.append( RtfControlWords.CELL_MERGE_FIRST );
    return this;
  }

  /**
   * Marks this cell as part of (but not the first of) a horizontally merged range.
   *
   * @return {@code this}-object.
   */
  public RtfCell mergeHorizontally() {
    celldef.append( RtfControlWords.CELL_MERGE );
    return this;
  }

  /**
   * Marks this cell as the first one of a vertically merged range of cells (spanning rows).
   * The cells below in the range use {@link #mergeVertically()}.
   *
   * @return {@code this}-object.
   */
  public RtfCell mergeVerticallyFirst() {
    celldef.append( RtfControlWords.CELL_VERTICAL_MERGE_FIRST );
    return this;
  }

  /**
   * Marks this cell as part of (but not the first of) a vertically merged range.
   *
   * @return {@code this}-object.
   */
  public RtfCell mergeVertically() {
    celldef.append( RtfControlWords.CELL_VERTICAL_MERGE );
    return this;
  }

  // Horizontal alignment of the cell content

  /**
   * Left-aligns the text of this cell (applies to the content paragraphs).
   *
   * @return {@code this}-object.
   */
  public RtfCell alignLeft() {
    for ( RtfPara para : paras )
      if ( para instanceof RtfTextPara )
        ((RtfTextPara) para).alignLeft();
    return this;
  }

  /**
   * Right-aligns the text of this cell (applies to the content paragraphs).
   *
   * @return {@code this}-object.
   */
  public RtfCell alignRight() {
    for ( RtfPara para : paras )
      if ( para instanceof RtfTextPara )
        ((RtfTextPara) para).alignRight();
    return this;
  }

  /**
   * Centers the text of this cell (applies to the content paragraphs).
   *
   * @return {@code this}-object.
   */
  public RtfCell alignCentered() {
    for ( RtfPara para : paras )
      if ( para instanceof RtfTextPara )
        ((RtfTextPara) para).alignCentered();
    return this;
  }

  /**
   * Justifies the text of this cell (applies to the content paragraphs).
   *
   * @return {@code this}-object.
   */
  public RtfCell alignJustified() {
    for ( RtfPara para : paras )
      if ( para instanceof RtfTextPara )
        ((RtfTextPara) para).alignJustified();
    return this;
  }

  /**
   * Makes the whole content of this cell bold.
   *
   * @return {@code this}-object.
   */
  public RtfCell bold() {
    paras.replaceAll( RtfCell::wrapBold );
    return this;
  }

  private static RtfPara wrapBold( RtfPara para ) {
    return RtfPara.of( ( out, withEndingPar ) -> {
      out.append( "{" ).append( RtfControlWords.BOLD ).append( ' ' );
      para.rtf( out, withEndingPar );
      out.append( '}' );
    } );
  }

  /**
   * Width in twips this cell contributes to the row's cumulative {@code \cellx} boundary:
   * the explicit width if set, otherwise {@link #DEFAULT_CELL_WIDTH_TWIPS}.
   *
   * @return Width in twips.
   */
  int effectiveWidthTwips() {
    return widthTwips >= 0 ? widthTwips : DEFAULT_CELL_WIDTH_TWIPS;
  }
}
