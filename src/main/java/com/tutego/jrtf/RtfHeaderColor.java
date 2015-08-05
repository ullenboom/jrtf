/*
 * Copyright (c) 2010-2015 Christian Ullenboom 
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

import java.io.IOException;

/**
 * Represents a color definition for the RTF header.
 */
public class RtfHeaderColor extends RtfHeader implements Comparable<RtfHeaderColor>
{
  /** Constant for color black. */
  public static final RtfHeaderColor BLACK = new RtfHeaderColor( 0, 0, 0 );

  /** Constant for color white. */
  public static final RtfHeaderColor WHITE = new RtfHeaderColor( 255, 255, 255 );

  /** Red, Green, Blue. */
  private int r, g, b;

  /** Index of the color. */
  int colorindex;

  /**
   * Package visible constructor. The user will not instantiate this class.
   * @param r  Red.
   * @param g  Green.
   * @param b  Blue.
   */
  RtfHeaderColor( int r, int g, int b )
  {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  /**
   * Sets a color at a certain index. The index has to be between 1 and 255 otherwise a {@code RtfException}
   * will be thrown. Index 0 is reserved for the AUTO color.
   * 
   * @param colorindex Index of the color.
   * @return {@link RtfHeader}
   */
  public RtfHeader at( int colorindex )
  {
    if ( colorindex < 1 || colorindex > 255 )
      throw new RtfException( "Color index " + colorindex  + " ist out of range, has to be between 1 and 255" );

    this.colorindex = colorindex;

    return this;
  }

  public int compareTo( RtfHeaderColor other )
  {
    return this.colorindex - other.colorindex;
  }

  /**
   * Appends the color definition of one color in RTF format.
   * @param out Appendable
   * @throws IOException
   */
  void writeColordef( Appendable out ) throws IOException
  {
    /*
     * <colordef> := \red ? & \green ? & \blue ? ';'
     */
    out.append( "\\red" )
       .append( Integer.toString( r ) )
       .append( "\\green" )
       .append( Integer.toString( g ) )
       .append( "\\blue" )
       .append( Integer.toString( b ) )
       .append( ';' );
  }  
}