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
 * Factory and base class that represents a RTF header.
 * Different subclasses defines specific headers for
 * <ul>
 * <li>font definitions
 * <li>color tables
 * <li>style sheets (not implemented yet)
 * </ul>
 */
public abstract class RtfHeader
{
  /**
   * Package visible constructor. The user will not instantiate this class.
   */
  RtfHeader() { }

  /**
   * Creates a new font definition.
   * @param fontname  Name of the font.
   * @return {@link RtfHeaderFont} to configure the font.
   */
  public static RtfHeaderFont font( String fontname )
  {
    return new RtfHeaderFont( fontname );
  }

  /**
   * Creates a new color definition.
   * From the color values just the last 8 bit are used.
   * @param r Red.
   * @param g Green.
   * @param b Blue.
   * @return {@link RtfHeaderColor} to set the position of the font.
   */
  public static RtfHeaderColor color( int r, int g, int b )
  {
    return new RtfHeaderColor( r & 0xFF, g & 0xFF, b & 0xFF );
  }

//  
//  /**
//   * The default style has to be on position 0.
//   */
//   public static RtfHeaderStyle defaultStyle()
//   {
//    return new RtfHeaderStyle( "", "" );
//   }
//   
//   public static RtfHeaderStyle parStyle( String stylename )
//   {
//    return new RtfHeaderStyle( "\\s", stylename );
//   }
//   
//   public static RtfHeaderStyle charStyle( String stylename )
//   {
//    return new RtfHeaderStyle( "\\cs", stylename );
//   }
}
