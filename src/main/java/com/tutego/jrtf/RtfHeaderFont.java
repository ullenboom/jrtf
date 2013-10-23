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

import java.io.IOException;

/**
 * Header definitions for font declarations.
 */
public class RtfHeaderFont extends RtfHeader
{
  /** Constant for Courier font. */
  public static final String COURIER = "Courier";

  /** Constant for Helvetica (Arial) font. */
  public static final String HELVETICA = "Arial";

  /** Constant for Courier font. */
  public static final String ARIAL = "Arial";

  /** Constant for Symbol font. */
  public static final String SYMBOL = "Symbol";

  /** Constant for Times font. */
  public static final String TIMES_ROMAN = "Times New Roman";
  
  /** Constant for Zapf Dingbats (Windings) font. */
  public static final String ZAPFDINGBATS = "Windings";
  
  /** Constant for Windings font. */
  public static final String WINDINGS = "Windings";

  /**
   * RTF font families.
   */
  public enum FontFamily
  {
    /** Unknown or default fonts (the default). */
    NIL,
    
    /** Roman, proportionally spaced serif fonts, e.g. Times New Roman, Palatino. */
    ROMAN,
    
    /** Swiss, proportionally spaced sans serif fonts, e.g. Arial. */
    SWISS,
    
    /** Fixed-pitch serif and sans serif fonts, e.g. Courier New, Pica. */
    MODERN,
    
    /** Script fonts, e.g. Cursive. */
    SCRIPT,
    
    /**  Decorative fonts, e.g. Old English, ITC Zapf Chancery. */ 
    DECOR,
    
    /** Technical, symbol, and mathematical fonts, e.g. Symbol. */
    TECH,
  
    /** Arabic, Hebrew, or other bidirectional font, e.g. Miriam. */
    BIDI
  }

  /**
   * Specifies the RTF character set of a font in the font table.
   */
  public enum CharSet
  {
    /** ANSI. */
    ANSI { @Override public String toString() { return "0"; } },
    
    /** Default. */
    DEFAULT { @Override public String toString() { return "1"; } },

    /** Symbol. */
    SYMBOL { @Override public String toString() { return "2"; } },

//    /** Invalid. */   Do we need this?
//    INVALID { @Override public String toString() { return "3"; } },

    /** Mac. */
    MAC { @Override public String toString() { return "77"; } },

    /** Shift Jis. */
    SHIFTJIS { @Override public String toString() { return "128"; } },

    /** Hangul. */
    HANGUL { @Override public String toString() { return "129"; } },

    /** Johab. */
    JOHAB { @Override public String toString() { return "130"; } },

    /** Greek. */
    GREEK { @Override public String toString() { return "161"; } },
    
    /** Turkish. */
    TURKISH { @Override public String toString() { return "162"; } },
    
    /** Vietnamese. */
    VIETNAMESE { @Override public String toString() { return "163"; } },

    /** Hebrew. */
    HEBREW { @Override public String toString() { return "177"; } },

    /** Arabic. */
    ARABICSIMPLIFIED { @Override public String toString() { return "178"; } },
    
    /** Arabic Traditional. */
    ARABICTRADITIONAL { @Override public String toString() { return "179"; } },
    
    /** Arabic user. */
    ARABICUSER { @Override public String toString() { return "180"; } },

    /** Hebrew user. */
    HEBREWUSER { @Override public String toString() { return "181"; } },

    /** Baltic. */
    BALTIC { @Override public String toString() { return "186"; } },
    
    /** Russian. */
    CYRILLIC { @Override public String toString() { return "204"; } },

    /** Thai. */
    THAI { @Override public String toString() { return "222"; } },

    /** Eastern European. */
    EASTERNEUROPE { @Override public String toString() { return "238"; } },

    /** PC 437. */
    PC437 { @Override public String toString() { return "254"; } },

    /** OEM. */
    OEM { @Override public String toString() { return "255"; } },
  }

  /**
   * Pitch of a font.
   */
  public enum Pitch
  {
    /** Default pitch. */
    DEFAULT { @Override public String toString() { return "0"; } },

    /** Fixed pitch. */
    FIXED { @Override public String toString() { return "1"; } },

    /** Variable pitch. */
    VARIABLE { @Override public String toString() { return "2"; } },
  }

  /** Font number of a font in the header. */
  private int fontnum;

  /** Name of the font. */
  private String fontname;

  /** Font family. */
  private FontFamily fontfamily = FontFamily.NIL;

  /** Char set of this font. */
  private CharSet charSet = CharSet.ANSI;

  /** Pitch of this font. */
  private Pitch pitch;

  /**
   * Package visible constructor. The user will not instantiate this class.
   * @param fontname 
   */
  RtfHeaderFont( String fontname )
  {
    this.fontname = fontname;
  }

  /**
   * Sets the font family.
   * @param fontfamily Font family.
   * @return {@code this}-object.
   */
  public RtfHeaderFont family( FontFamily fontfamily )
  {
    if ( fontfamily == null )
      throw new IllegalArgumentException( "Font family can't be null" );
    
    this.fontfamily = fontfamily;
    return this;
  }

  /**
   * Sets the char set.
   * @param charSet Char set.
   * @return {@code this}-object.
   */
  public RtfHeaderFont charset( CharSet charSet )
  {
    if ( charSet == null )
      throw new IllegalArgumentException( "Char set can't be null" );
    
    this.charSet = charSet;
    return this;
  }

  /**
   * Sets the pitch.
   * @param pitch Pitch of this font.
   * @return {@code this}-object.
   */
  public RtfHeaderFont pitch( Pitch pitch )
  {
    if ( pitch == null )
      throw new IllegalArgumentException( "Pitch can't be null" );

    this.pitch = pitch;
    return this;
  }

  /**
   * Sets the number of this fonts in the header and finishes the header font definition.
   * @param fontnum Number of the font.
   * @return {@link RtfHeader}.
   */
  public RtfHeader at( int fontnum )
  {
    if ( fontnum < 0 )
      throw new IllegalArgumentException( "Font number is not allowed to be negative" );
    
    this.fontnum = fontnum;
    return this;
  }

  /**
   * Writes out the RTF definition for a font. 
   * @param out
   * @throws IOException
   */
  void writeFontInfo( Appendable out ) throws IOException
  {
    /*
     * <fontinfo> := <fontnum>
     *               <fontfamily>
     *               <fcharset>?
     *               <fprq>?
     *               <panose>?
     *               <nontaggedname>?
     *               <fontemb>?
     *               <codepage>?
     *               <fontname>
     *               <fontaltname>? ';'
     *               
     * <fontnum>    := \f
     * <fontfamily> := \fnil | \froman | \fswiss | \fmodern | \fscript | \fdecor | \ftech | \fbidi
     * <fcharset>   := \fcharset
     * <fprq>       := \fprq
     * <fontname>   := #PCDATA
     */
    
    out.append( "{\\f" ).append( Integer.toString( fontnum ) )
       .append( "\\f" ).append( fontfamily.toString().toLowerCase() )
       .append( (charSet != null ? "\\fcharset" + charSet : "" ) )
       .append( (pitch != null ? "\\fprq" + pitch : "" ) )
       .append( " " ).append( fontname ).append( ";}" );        
  }
}
