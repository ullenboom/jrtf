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
 * Defines properties of the hole row. The properties for a row, e.g. borders,
 * are assigned to every cell.
 */
public class RtfRow extends RtfPara {
  /*
   * <row>   :=  <tbldef> <cell>+ \row
   * <cell>  :=  <textpar>+ \cell
   */

  /**
   * Collects the RTF for the table definitions.
   */
  final StringBuilder tbldef = new StringBuilder();

  Consumer<RtfOutput> renderer;

  @Override void rtf( RtfOutput out, boolean withEndingPar ) {
    if ( renderer != null )
      renderer.accept( out );
  }

  /**
   * Applies a registered table style to this row.
   *
   * @param style Table style previously registered with {@link Rtf#tableStyles(RtfTableStyle...)}.
   * @return {@code this}-object.
   */
  public RtfRow tableStyle( RtfTableStyle style ) {
    if ( style.getId() < 0 )
      throw new IllegalArgumentException( "Table style must be registered first. "
          + "Register it with Rtf.tableStyles(style) before using it here." );
    tbldef.append( '\\' ).append( RtfControlWords.TABLE_STYLE_DEFINITION ).append( style.getId() );
    return this;
  }

  // Row Formatting

  /**
   * Keeps this row together with the following row (no page break between them).
   *
   * @return {@code this}-object.
   */
  public RtfRow keepWithNext() {
    tbldef.append( '\\' ).append( RtfControlWords.KEEP_WITH_NEXT ).append( '\n' );
    return this;
  }

  /**
   * Turns AutoFit on for the row, so the cell widths adapt to their content. AutoFit is off
   * by default (the {@code \cellx} boundaries are then authoritative).
   *
   * @return {@code this}-object.
   */
  public RtfRow autoFit() {
    tbldef.append( '\\' ).append( RtfControlWords.ROW_AUTOFIT ).append( '1' );
    return this;
  }

  /**
   * Marks this row as a header row that repeats at the top of every page the table spans.
   *
   * @return {@code this}-object.
   */
  public RtfRow repeatAsHeaderRow() {
    tbldef.append( '\\' ).append( RtfControlWords.ROW_HEADER_REPEAT );
    return this;
  }

  /**
   * Table direction is right to left.
   *
   * @return {@code this}-object.
   */
  public RtfRow rightToLeft() {
    tbldef.append( '\\' ).append( RtfControlWords.ROW_RIGHT_TO_LEFT );
    return this;
  }

  /**
   * Default top cell padding (margin) applied to every cell of the row.
   *
   * @param margin Padding (its absolute value is used).
   * @param unit   Measurement unit.
   * @return {@code this}-object.
   */
  public RtfRow topCellMargin( double margin, RtfUnit unit ) {
    tbldef.append( '\\' ).append( RtfControlWords.CELL_PADDING_UNIT_TOP ).append( '3' )
          .append( '\\' ).append( RtfControlWords.CELL_PADDING_TOP ).append( unit.toTwips( Math.abs( margin ) ) );
    return this;
  }

  /**
   * Default bottom cell padding (margin) applied to every cell of the row.
   *
   * @param margin Padding (its absolute value is used).
   * @param unit   Measurement unit.
   * @return {@code this}-object.
   */
  public RtfRow bottomCellMargin( double margin, RtfUnit unit ) {
    tbldef.append( '\\' ).append( RtfControlWords.CELL_PADDING_UNIT_BOTTOM ).append( '3' )
          .append( '\\' ).append( RtfControlWords.CELL_PADDING_BOTTOM ).append( unit.toTwips( Math.abs( margin ) ) );
    return this;
  }

  /**
   * Default left cell padding (margin) applied to every cell of the row.
   *
   * @param margin Padding (its absolute value is used).
   * @param unit   Measurement unit.
   * @return {@code this}-object.
   */
  public RtfRow leftCellMargin( double margin, RtfUnit unit ) {
    tbldef.append( '\\' ).append( RtfControlWords.CELL_PADDING_UNIT_LEFT ).append( '3' )
          .append( '\\' ).append( RtfControlWords.CELL_PADDING_LEFT ).append( unit.toTwips( Math.abs( margin ) ) );
    return this;
  }

  /**
   * Default right cell padding (margin) applied to every cell of the row.
   *
   * @param margin Padding (its absolute value is used).
   * @param unit   Measurement unit.
   * @return {@code this}-object.
   */
  public RtfRow rightCellMargin( double margin, RtfUnit unit ) {
    tbldef.append( '\\' ).append( RtfControlWords.CELL_PADDING_UNIT_RIGHT ).append( '3' )
          .append( '\\' ).append( RtfControlWords.CELL_PADDING_RIGHT ).append( unit.toTwips( Math.abs( margin ) ) );
    return this;
  }

  // Cell Borders

  /**
   * Bottom table cell border.
   *
   * @return {@code this}-object.
   */
  public RtfRow bottomCellBorder() {
    tbldef.append( '\\' ).append( RtfControlWords.CELL_BORDER_BOTTOM ).append( '\\' ).append( RtfControlWords.BORDER_SINGLE );
    return this;
  }

  /**
   * Top table cell border.
   *
   * @return {@code this}-object.
   */
  public RtfRow topCellBorder() {
    tbldef.append( '\\' ).append( RtfControlWords.CELL_BORDER_TOP ).append( '\\' ).append( RtfControlWords.BORDER_SINGLE );
    return this;
  }

  /**
   * Left table cell border.
   *
   * @return {@code this}-object.
   */
  public RtfRow leftCellBorder() {
    tbldef.append( '\\' ).append( RtfControlWords.CELL_BORDER_LEFT ).append( '\\' ).append( RtfControlWords.BORDER_SINGLE );
    return this;
  }

  /**
   * Right table cell border.
   *
   * @return {@code this}-object.
   */
  public RtfRow rightCellBorder() {
    tbldef.append( '\\' ).append( RtfControlWords.CELL_BORDER_RIGHT ).append( '\\' ).append( RtfControlWords.BORDER_SINGLE );
    return this;
  }

  /**
   * Half the space between the cells of a table row.
   *
   * @param space Space between cells.
   * @param unit  Measurement.
   * @return {@code this}-object.
   */
  public RtfRow cellSpace( double space, RtfUnit unit ) {
    tbldef.append( '\\' ).append( RtfControlWords.ROW_GAP ).append( unit.toTwips( space ) );
    return this;
  }

  /**
   * Defines the cell height.
   *
   * @param height Height of the cell.
   * @param unit   Measurement.
   * @return {@code this}-object.
   */
  public RtfRow cellHeight( double height, RtfUnit unit ) {
    tbldef.append( '\\' ).append( RtfControlWords.ROW_HEIGHT ).append( unit.toTwips( height ) );
    return this;
  }
}
