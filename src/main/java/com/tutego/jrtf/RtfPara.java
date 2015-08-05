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
import java.util.*;

/**
 * Build a paragraph or a table row.
 */
public abstract class RtfPara
{
  /**
   * <section> := <secfmt>* <hdrftr>? <para>+ (\sect <section>)?
   * <para>    := <textpar> | <row>
   * 
   * <textpar> := <pn>? <brdrdef>? <parfmt>* <apoctl>* <tabdef>? <shading>? (\subdocument | <char>+) (\par <para>)?
   * 
   * <row>     := <tbldef> <cell>+ \row
   * <cell>    := <textpar>+ \cell
   * 
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
   * Writes out pure RTF of this paragraph into an {@link Appendable}.
   * @param out Output.
   * @param withEndingPar  Output will write an \par at the end.
   */
  abstract void rtf( Appendable out, boolean withEndingPar ) throws IOException;

  /**
   * Builds a paragraph of objects (that will be converted to Strings and {@code RtfText}).
   * Convenience method for {@code p(RtfText.text(texts))}.
   * @param texts Text to set in paragraph.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara p( Object... texts )
  {
    return p( RtfText.text( texts ) );
  }
  
  /**
   * Builds a paragraph of objects (that will be converted to Strings and {@code RtfText}).
   * Convenience method for {@code p(RtfText.text(texts))} with an associated style.
   * @param style Style sheet to set in paragraph.
   * @param texts Text to set in paragraph.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara p( RtfHeaderStyle style, Object... texts )
  {
    return p( style, RtfText.text( texts ) );
  }

  /**
   * A paragraph with a collection of text. This paragraph will inherit all
   * the settings from the other paragraph. See {@link #pard(RtfText...)} if you
   * look for a method where paragraph attributes are not inherited to the next
   * paragraph.
   * @param texts Text to set in paragraph.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara p( final RtfText... texts )
  {
    return p( RtfHeaderStyle.NORMAL , texts );
  }

  /**
   * A paragraph with a collection of text. This paragraph will inherit all
   * the settings from the other paragraph. See {@link #pard(RtfText...)} if you
   * look for a method where paragraph attributes are not inherited to the next
   * paragraph.
   * @param style Style sheet to set in paragraph.
   * @param texts Text to set in paragraph.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara p( final RtfHeaderStyle style , final RtfText... texts )
  {
    if ( texts == null || texts.length == 0 )
      return new RtfTextPara() {
        @Override void rtf( Appendable out, boolean withEndingPar ) throws IOException {
          out.append( "\\par" );
        }
      };
      
    return new RtfTextPara() {
      @Override void rtf( Appendable out, boolean withEndingPar ) throws IOException {
        out.append( "{" );
        out.append( "\\s" ).append( Integer.toString( style.getId() ) );
        out.append( textparFormatRtf() );

        for ( RtfText rtfText : texts )
          rtfText.rtf( out );

        if ( withEndingPar )     // if its in table, withEndingPar will be false
          out.append( "\\par" );

        out.append( "}\n" );
      }
    };
  }

  /**
   * A paragraph with a collection of text. This paragraph will not inherit the
   * settings from the other paragraph. See {@link #p(RtfText...)} if you
   * look for a method where paragraph attributes are inherited to the next
   * paragraph.
   * @param texts Text to set in paragraph.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara pard( final RtfText... texts )
  {
    return pard( RtfHeaderStyle.NORMAL , texts );
  }

  /**
   * A paragraph with a collection of text. This paragraph will not inherit the
   * settings from the other paragraph. See {@link #p(RtfText...)} if you
   * look for a method where paragraph attributes are inherited to the next
   * paragraph.
   * @param style Style sheet to set in paragraph.
   * @param texts Text to set in paragraph.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara pard( final RtfHeaderStyle style, final RtfText... texts )
  {
    if ( texts == null || texts.length == 0 )
      return new RtfTextPara() {
        @Override void rtf( Appendable out, boolean withEndingPar ) throws IOException {
          out.append( "\\pard\\par" );
      }
    };
    
    return new RtfTextPara() {
      @Override void rtf( Appendable out, boolean withEndingPar ) throws IOException {
        out.append( "{\\pard" );
        out.append( "\\s" ).append( Integer.toString( style.getId() ) );
        out.append( textparFormatRtf() );

        for ( RtfText rtfText : texts )
          rtfText.rtf( out );

        if ( withEndingPar )     // if its in table, withEndingPar will be false
          out.append( "\\par" );

        out.append( "}\n" );
      }
    };
  }

  /**
   * Text is in a unordered list with a preceding bullet.
   * @param text Text to set in paragraph.
   * @return New {@code RtfPara} object with text and bullet.
   */
  public static RtfPara ul( final String text )
  {
    return ul( new RtfText( text ) );
  }

  /**
   * Text is in a unordered list with a preceding bullet.
   * @param text Text to set in paragraph.
   * @return New {@code RtfPara} object with text and bullet.
   */
  public static RtfPara ul( final RtfText text )
  {
    final String s = "{\\pard{\\pntext\\bullet\\tab}{\\*\\pn\\pnlvlblt\\pnf1\\pnindent0{\\pntxtb\\bullet}}\\fi-200\\li200 ";

    return new RtfPara() {
      @Override void rtf( Appendable out, boolean withEndingPar ) throws IOException {
        out.append( s );
        text.rtf( out );
        out.append( '}' );
      }
    };
  }

  /**
   * Writes a row with a sequence of text cells.
   * @param cells Cells of the row.
   * @return New row object.
   */
  public static RtfRow row( RtfText... cells )
  {
    return rowWithBackgroundColor( 0, cells );
  }

  /**
   * Writes a row with a sequence of text cells.
   * @param colorIndex Row background color.
   * @param cells Cells of the row.
   * @return New row object.
   */
  public static RtfRow rowWithBackgroundColor( int colorIndex, RtfText... cells )
  {
    if ( cells == null )
      throw new RtfException( "There has to be at least one cell in a row" );

    List<RtfPara> paras = new ArrayList<RtfPara>();
    for ( RtfText cell : cells )
      paras.add( p(cell) );

    RtfPara[] parasArray = new RtfPara[ paras.size() ];
    return rowWithBackgroundColor( colorIndex, paras.toArray( parasArray ) );
  }

  /**
   * Writes a row with a sequence of text cells. Every object is
   * converted to String and wrapped to a paragraph with the
   * method {@link RtfPara#p(Object...)}.
   * @param cells Cells of the row.
   * @return New row object.
   */
  public static RtfRow row( Object... cells )
  {
    return rowWithBackgroundColor( 0, cells );
  }

  /**
   * Writes a row with a sequence of text cells. Every object is
   * converted to String and wrapped to a paragraph with the
   * method {@link RtfPara#p(Object...)}.
   * @param colorIndex Row background color.
   * @param cells Cells of the row.
   * @return New row object.
   */
  public static RtfRow rowWithBackgroundColor( int colorIndex, Object... cells )
  {
    if ( cells == null || cells.length == 0 )
      throw new RtfException( "There has to be at least one cell in a row" );

    List<RtfPara> paras = new ArrayList<RtfPara>();
    for ( Object cell : cells )
    {
      if ( cell instanceof RtfPara )
        paras.add( (RtfPara) cell );
      else
        paras.add( p(cell) );
    }

    RtfPara[] parasArray = new RtfPara[ paras.size() ];
    return rowWithBackgroundColor( colorIndex, paras.toArray( parasArray ) );
  }
  
  /**
   * Writes a row with a sequence of paragraph cells.
   * @param cells Cells of the row.
   * @return New object for the row.
   */
  public static RtfRow row( RtfPara... cells )
  {
	  return rowWithBackgroundColor( 0, cells );
  }

  /**
   * Writes a row with a sequence of paragraph cells.
   * @param colorIndex Row background color.
   * @param cells Cells of the row.
   * @return New object for the row.
   */
  public static RtfRow rowWithBackgroundColor( final int colorIndex, final RtfPara... cells )
  {
    /* <row>    := <tbldef> <cell>+ \row
     * <cell>   := <textpar>+ \cell
     * 
     * <tbldef> := \trowd \trgaph <rowjust>? & <rowwrite>? & \trleft? \trheader? & \trkeep? <celldef>+
     */
    if ( cells == null )
      throw new RtfException( "There has to be at least one cell in a row" );

    return new RtfRow() {
      @Override void rtf( Appendable out, boolean withEndingPar ) throws IOException {
        out.append( "{\\trowd\\trautofit1\\intbl\n" );
        for ( int i = 1; i <= cells.length; i++ )
          out.append( tbldef )
             .append( (cells[ i - 1 ] instanceof RtfTextPara) ? ((RtfTextPara) cells[i-1]).cellfmt : "" )
             .append( "\\clcbpat" ).append( Integer.toString( colorIndex ) )
             .append( "\\cellx" )
             .append( Integer.toString( i ) ).append( '\n' );

        for ( RtfPara cell : cells )
        {
          cell.rtf( out, false );
          out.append( "\\cell\n" );
        }
        out.append( "\\row}" );
      }
    };
  }
}
