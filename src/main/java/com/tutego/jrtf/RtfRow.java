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
 * Defines properties of the hole row. The properties for a row, e.g. borders,
 * are assigned to every cell.
 */
public abstract class RtfRow extends RtfPara
{
  /*
   * <row>   :=  <tbldef> <cell>+ \row
   * <cell>  :=  <textpar>+ \cell
   */

  /** Collects the RTF for the table definitions. */
  StringBuilder tbldef = new StringBuilder();

  // Row Formatting

  //  /**
  //   * AutoFit is on for the row. No auto fit is the default.
  //   * @return {@code this}-object.
  //   */
  //  public RtfRow autoFit()
  //  {
  //    tbldef.append( "\\trautofit1" );
  //    return this;
  //  }

  /**
   * Table direction is right to left.
   * @return {@code this}-object.
   */
  public RtfRow rightToLeft()
  {
    tbldef.append( "\\taprtl" );
    return this;
  }

//  /**
//   * Default bottom cell margin or padding for the row.
//   * @param margin
//   * @param unit
//   * @return
//   */
//  public RtfRow bottomCellMargin( double margin, RtfUnit unit )
//  {
//    tbldef.append( "\\trpaddfb3\\trpaddb" ).append( unit.toTwips( margin ) );
//    return this;
//  }
//
//  /**
//   * Default left cell margin or padding for the row.
//   * @param margin
//   * @param unit
//   * @return
//   */
//  public RtfRow leftCellMargin( double margin, RtfUnit unit )
//  {
//    tbldef.append( "\\trpaddl3\\trpaddl" ).append( unit.toTwips( margin ) );
//    return this;
//  }
//
//  /**
//   * Default right cell margin or padding for the row.
//   * @param margin
//   * @param unit
//   * @return
//   */
//  public RtfRow rightCellMargin( double margin, RtfUnit unit )
//  {
//    tbldef.append( "\\trpaddr3\\trpaddr" ).append( unit.toTwips( margin ) );
//    return this;
//  }
//
//  /**
//   * Default top cell margin or padding for the row.
//   * @param margin
//   * @param unit
//   * @return
//   */
//  public RtfRow topCellMargin( double margin, RtfUnit unit )
//  {
//    tbldef.append( "\\trpaddt3\\trpaddt" ).append( unit.toTwips( margin ) );
//    return this;
//  }

  // Cell Borders
  
  /**
   * Bottom table cell border.
   * @return {@code this}-object.
   */
  public RtfRow bottomCellBorder()
  {
    tbldef.append( "\\clbrdrb\\brdrs" );
    return this;
  }

  /**
   * Top table cell border.
   * @return {@code this}-object.
   */
  public RtfRow topCellBorder()
  {
    tbldef.append( "\\clbrdrt\\brdrs" );
    return this;
  }

  /**
   * Left table cell border.
   * @return {@code this}-object.
   */
  public RtfRow leftCellBorder()
  {
    tbldef.append( "\\clbrdrl\\brdrs" );
    return this;
  }

  /**
   * Right table cell border.
   * @return {@code this}-object.
   */
  public RtfRow rightCellBorder()
  {
    tbldef.append( "\\clbrdrr\\brdrs" );
    return this;
  }

  /**
   * Half the space between the cells of a table row.
   * @param space Space between cells.
   * @param unit Measurement.
   * @return {@code this}-object.
   */
  public RtfRow cellSpace( double space, RtfUnit unit )
  {
    tbldef.append( "\\trgaph" ).append( unit.toTwips( space ) );
    return this;
  }

  /**
   * Defines the cell height.
   * @param height Height of the cell.
   * @param unit Measurement.
   * @return {@code this}-object.
   */
  public RtfRow cellHeight( double height, RtfUnit unit )
  {
    tbldef.append( "\\trrh" ).append( unit.toTwips( height ) );
    return this;
  }  
}
