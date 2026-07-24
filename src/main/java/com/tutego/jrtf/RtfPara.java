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
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Build a paragraph or a table row.
 */
public abstract class RtfPara {
  /*
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
   *
   * @param out           Output.
   * @param withEndingPar Output will write an \par at the end.
   */
  abstract void rtf( Appendable out, boolean withEndingPar ) throws IOException;

  /**
   * Functional interface for the render body of a {@code RtfPara}, so callers
   * can pass a lambda instead of subclassing {@code RtfPara} anonymously.
   */
  @FunctionalInterface
  interface Renderer {
    void rtf( Appendable out, boolean withEndingPar ) throws IOException;
  }

  /**
   * Wraps a {@link Renderer} lambda into a {@code RtfPara}.
   *
   * @param renderer Render body, evaluated only when the enclosing document is written.
   * @return New {@code RtfPara} object delegating to {@code renderer}.
   */
  static RtfPara of( Renderer renderer ) {
    return new RtfPara() {
      @Override void rtf( Appendable out, boolean withEndingPar ) throws IOException {
        renderer.rtf( out, withEndingPar );
      }
    };
  }

  /**
   * Builds a paragraph of objects (that will be converted to Strings and {@code RtfText}).
   * Convenience method for {@code p(RtfText.text(texts))}.
   *
   * @param texts Text to set in paragraph. See {@link RtfText#text(Object...)} for how each
   *              element is resolved.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara p( Object... texts ) {
    return p( RtfText.text( texts ) );
  }

  /**
   * Builds a paragraph of objects (that will be converted to Strings and {@code RtfText}).
   * Convenience method for {@code p(RtfText.text(texts))} with an associated style.
   *
   * @param style Style sheet to set in paragraph.
   * @param texts Text to set in paragraph. See {@link RtfText#text(Object...)} for how each
   *              element is resolved.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara p( RtfHeaderStyle style, Object... texts ) {
    return p( style, RtfText.text( texts ) );
  }

  /**
   * A paragraph with a collection of text. This paragraph will inherit all
   * the settings from the other paragraph. See {@link #pard(RtfText...)} if you
   * look for a method where paragraph attributes are not inherited to the next
   * paragraph.
   *
   * @param texts Text to set in paragraph. May be {@code null} or empty, in which case
   *              an empty paragraph is produced; individual elements must not be {@code null}.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara p( RtfText @Nullable ... texts ) {
    return p( RtfHeaderStyle.NORMAL, texts );
  }

  /**
   * A paragraph with a collection of text. This paragraph will inherit all
   * the settings from the other paragraph. See {@link #pard(RtfText...)} if you
   * look for a method where paragraph attributes are not inherited to the next
   * paragraph.
   *
   * @param style Style sheet to set in paragraph.
   * @param texts Text to set in paragraph. May be {@code null} or empty, in which case
   *              an empty paragraph is produced; individual elements must not be {@code null}.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara p( RtfHeaderStyle style, RtfText @Nullable ... texts ) {
    if ( texts == null || texts.length == 0 )
      return RtfTextPara.of( ( self, out, withEndingPar ) -> out.append( RtfControlWords.PAR ) );

    return RtfTextPara.of( ( self, out, withEndingPar ) -> {
      out.append( "{" );
      out.append( RtfControlWords.STYLE ).append( Integer.toString( style.getId() ) ).append( " " );
      out.append( self.textparFormatRtf() );

      for ( RtfText rtfText : texts )
        rtfText.rtf( out );

      if ( withEndingPar )     // if its in table, withEndingPar will be false
        out.append( RtfControlWords.PAR );

      out.append( "}\n" );
    } );
  }

  /**
   * A paragraph with a collection of text. This paragraph will not inherit the
   * settings from the other paragraph. See {@link #p(RtfText...)} if you
   * look for a method where paragraph attributes are inherited to the next
   * paragraph.
   *
   * @param texts Text to set in paragraph. May be {@code null} or empty, in which case
   *              an empty paragraph is produced; individual elements must not be {@code null}.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara pard( RtfText @Nullable ... texts ) {
    return pard( RtfHeaderStyle.NORMAL, texts );
  }

  /**
   * A paragraph with a collection of text. This paragraph will not inherit the
   * settings from the other paragraph. See {@link #p(RtfText...)} if you
   * look for a method where paragraph attributes are inherited to the next
   * paragraph.
   *
   * @param style Style sheet to set in paragraph.
   * @param texts Text to set in paragraph. May be {@code null} or empty, in which case
   *              an empty paragraph is produced; individual elements must not be {@code null}.
   * @return New {@code RtfTextPara} object with text.
   */
  public static RtfTextPara pard( RtfHeaderStyle style, RtfText @Nullable ... texts ) {
    if ( texts == null || texts.length == 0 )
      return RtfTextPara.of( ( self, out, withEndingPar ) ->
          out.append( RtfControlWords.PARAGRAPH_DEFAULTS ).append( RtfControlWords.PAR ) );

    return RtfTextPara.of( ( self, out, withEndingPar ) -> {
      out.append( "{" ).append( RtfControlWords.PARAGRAPH_DEFAULTS );
      out.append( RtfControlWords.STYLE ).append( Integer.toString( style.getId() ) ).append( " " );
      out.append( self.textparFormatRtf() );

      for ( RtfText rtfText : texts )
        rtfText.rtf( out );

      if ( withEndingPar )     // if its in table, withEndingPar will be false
        out.append( RtfControlWords.PAR );

      out.append( "}\n" );
    } );
  }

  /**
   * Text is in a unordered list with a preceding bullet.
   *
   * @param text Text to set in paragraph.
   * @return New {@code RtfPara} object with text and bullet.
   */
  public static RtfPara ul( String text ) {
    return ul( new RtfText( text ) );
  }

  /**
   * Text is in a unordered list with a preceding bullet.
   *
   * @param text Text to set in paragraph.
   * @return New {@code RtfPara} object with text and bullet.
   */
  public static RtfPara ul( RtfText text ) {
    final String s =
        "{" + RtfControlWords.PARAGRAPH_DEFAULTS
      + "{" + RtfControlWords.PN_TEXT + RtfControlWords.BULLET + RtfControlWords.TAB + "}"
      + "{" + RtfControlWords.DESTINATION_MARKER + RtfControlWords.PARAGRAPH_NUMBERING
            + RtfControlWords.PN_LEVEL_BULLET + RtfControlWords.PN_FONT + "1"
            + RtfControlWords.PN_INDENT + "0"
            + "{" + RtfControlWords.PN_TEXT_BEFORE + RtfControlWords.BULLET + "}}"
      + RtfControlWords.FIRST_LINE_INDENT + "-200" + RtfControlWords.LEFT_INDENT + "200 ";

    return RtfPara.of( ( out, withEndingPar ) -> {
      out.append( s );
      text.rtf( out );

      if ( withEndingPar )
        out.append( RtfControlWords.PAR );

      out.append( '}' );
    } );
  }

  /**
   * Text is in a unordered list with a preceding bullet with a hanging indent.
   *
   * @param text              Text to set in paragraph.
   * @param beforeBulletWidth Space between the left edge and the bullet
   * @param beforeBulletUnit  Unit of {@code beforeBulletWidth}
   * @param indentUnit        Unit of {@code indentWidth}
   * @return New {@code RtfPara} object with text and bullet.
   */
  public static RtfPara hangingUl( RtfText text, double beforeBulletWidth,
                                   RtfUnit beforeBulletUnit, double indentWidth, RtfUnit indentUnit,
                                   double afterItemSpace, RtfUnit afterItemUnit ) {
    return hangingUl( new RtfText( RtfControlWords.BULLET ), text, beforeBulletWidth, beforeBulletUnit,
                      indentWidth, indentUnit, afterItemSpace, afterItemUnit );
  }

  /**
   * Text is in a unordered list with a preceding bullet with a hanging indent.
   *
   * @param text              Text to set in paragraph.
   * @param beforeBulletWidth Space between the left edge and the bullet
   * @param beforeBulletUnit  Unit of {@code beforeBulletWidth}
   * @param indentUnit        Unit of {@code indentWidth}
   * @return New {@code RtfPara} object with text and bullet.
   */
  public static RtfPara hangingUl( RtfText bulletText, RtfText text,
                                   double beforeBulletWidth, RtfUnit beforeBulletUnit,
                                   double indentWidth, RtfUnit indentUnit,
                                   double afterItemSpace, RtfUnit afterItemUnit ) {
    int beforeTwips = beforeBulletUnit.toTwips( beforeBulletWidth );
    int indentTwips = indentUnit.toTwips( indentWidth );
    final int afterItemTwips = afterItemUnit.toTwips( afterItemSpace );
    final String s =
        String.format(
            "{%s" + RtfControlWords.LEFT_INDENT + "%d" + RtfControlWords.FIRST_LINE_INDENT + "-%d",
            (afterItemTwips != 0 ? (RtfControlWords.SPACE_AFTER + afterItemTwips) : ""),
            indentTwips, (indentTwips - beforeTwips) );

    return RtfPara.of( ( out, withEndingPar ) -> {
      out.append( s );
      out.append( "{" );
      bulletText.rtf( out );
      out.append( RtfControlWords.TAB );
      out.append( "}" );
      text.rtf( out );

      if ( withEndingPar )
        out.append( RtfControlWords.PAR );

      out.append( "}" );
    } );
  }

  /**
   * Numbering scheme of an ordered list item, see {@link #ol(ListNumbering, int, Object)}.
   */
  public enum ListNumbering {
    /**
     * Arabic numerals: 1, 2, 3, &hellip;
     */
    DECIMAL( RtfControlWords.PN_DECIMAL ),

    /**
     * Lowercase letters: a, b, c, &hellip;
     */
    LOWER_LETTER( RtfControlWords.PN_LOWER_LETTER ),

    /**
     * Uppercase letters: A, B, C, &hellip;
     */
    UPPER_LETTER( RtfControlWords.PN_UPPER_LETTER ),

    /**
     * Lowercase roman numerals: i, ii, iii, &hellip;
     */
    LOWER_ROMAN( RtfControlWords.PN_LOWER_ROMAN ),

    /**
     * Uppercase roman numerals: I, II, III, &hellip;
     */
    UPPER_ROMAN( RtfControlWords.PN_UPPER_ROMAN );

    private final String controlWord;

    ListNumbering( String controlWord ) {
      this.controlWord = controlWord;
    }
  }

  /**
   * A decimally numbered ({@code 1.}, {@code 2.}, &hellip;) ordered-list item, starting at 1.
   * Consecutive {@code ol(...)} paragraphs are numbered continuously by the word processor.
   * Convenience method for {@code ol(ListNumbering.DECIMAL, 1, RtfText.text(texts))}.
   *
   * @param texts Item text. Objects are converted like {@link RtfText#text(Object...)}.
   * @return New {@code RtfPara} object for the list item.
   */
  public static RtfPara ol( Object... texts ) {
    return ol( ListNumbering.DECIMAL, 1, RtfText.text( texts ) );
  }

  /**
   * An ordered-list item with the given numbering scheme, starting at 1.
   * Convenience method for {@code ol(numbering, 1, text)}.
   *
   * @param numbering Numbering scheme. Must not be {@code null}.
   * @param text      Item text. Converted like {@link RtfText#text(Object...)}; may be {@code null}
   *                  (treated like an empty text).
   * @return New {@code RtfPara} object for the list item.
   */
  public static RtfPara ol( ListNumbering numbering, @Nullable Object text ) {
    return ol( numbering, 1, text );
  }

  /**
   * An ordered-list item with the given numbering scheme and start number. Consecutive
   * list-item paragraphs are numbered continuously by the word processor; the start number
   * only takes effect on the first item of a list.
   *
   * @param numbering Numbering scheme. Must not be {@code null}.
   * @param start     Number of the first item (typically 1).
   * @param text      Item text. Converted like {@link RtfText#text(Object...)}; may be {@code null}
   *                  (treated like an empty text).
   * @return New {@code RtfPara} object for the list item.
   */
  public static RtfPara ol( ListNumbering numbering, int start, @Nullable Object text ) {
    if ( numbering == null )
      throw new IllegalArgumentException( "Numbering scheme can't be null" );

    final RtfText rtfText = RtfText.text( text );

    return RtfPara.of( ( out, withEndingPar ) -> {
      out.append( "{" ).append( RtfControlWords.PARAGRAPH_DEFAULTS )
         .append( "{" ).append( RtfControlWords.PN_TEXT ).append( RtfControlWords.TAB ).append( "}" )
         .append( "{" ).append( RtfControlWords.DESTINATION_MARKER ).append( RtfControlWords.PARAGRAPH_NUMBERING )
         .append( RtfControlWords.PN_LEVEL_BODY ).append( numbering.controlWord )
         .append( RtfControlWords.PN_START ).append( Integer.toString( start ) )
         .append( RtfControlWords.PN_INDENT ).append( "360" )
         .append( "{" ).append( RtfControlWords.PN_TEXT_AFTER ).append( ".}}" )
         .append( RtfControlWords.FIRST_LINE_INDENT ).append( "-360" )
         .append( RtfControlWords.LEFT_INDENT ).append( "360" ).append( " " );
      rtfText.rtf( out );
      if ( withEndingPar )
        out.append( RtfControlWords.PAR );
      out.append( "}" );
    } );
  }

  /**
   * Writes a row with a sequence of text cells.
   *
   * @param cells Cells of the row. Must not be {@code null}.
   * @return New row object.
   */
  public static RtfRow row( RtfText... cells ) {
    return rowWithBackgroundColor( 0, cells );
  }

  /**
   * Writes a row with a sequence of text cells.
   *
   * @param colorIndex Row background color.
   * @param cells      Cells of the row. Must not be {@code null}.
   * @return New row object.
   */
  public static RtfRow rowWithBackgroundColor( int colorIndex, RtfText... cells ) {
    if ( cells == null )
      throw new RtfException( "There has to be at least one cell in a row" );

    List<RtfPara> paras = new ArrayList<>();
    for ( RtfText cell : cells )
      paras.add( p( cell ) );

    RtfPara[] parasArray = new RtfPara[ paras.size() ];
    return rowWithBackgroundColor( colorIndex, paras.toArray( parasArray ) );
  }

  /**
   * Writes a row with a sequence of cells, one per element of {@code cells}. Unlike
   * {@link RtfText#text(Object...)}, an {@link RtfPara} element here is <em>not</em> rejected but
   * used directly as that cell's content; every other object (including {@link RtfText}) is wrapped
   * into a cell paragraph with {@link RtfPara#p(Object...)} (which in turn follows
   * {@link RtfText#text(Object...)}'s resolution rules).
   *
   * @param cells Cells of the row. Must not be {@code null} or empty.
   * @return New row object.
   */
  public static RtfRow row( Object... cells ) {
    return rowWithBackgroundColor( 0, cells );
  }

  /**
   * Writes a row with a sequence of cells, one per element of {@code cells}. Unlike
   * {@link RtfText#text(Object...)}, an {@link RtfPara} element here is <em>not</em> rejected but
   * used directly as that cell's content; every other object (including {@link RtfText}) is wrapped
   * into a cell paragraph with {@link RtfPara#p(Object...)} (which in turn follows
   * {@link RtfText#text(Object...)}'s resolution rules).
   *
   * @param colorIndex Row background color.
   * @param cells      Cells of the row. Must not be {@code null} or empty.
   * @return New row object.
   */
  public static RtfRow rowWithBackgroundColor( int colorIndex, Object... cells ) {
    if ( cells == null || cells.length == 0 )
      throw new RtfException( "There has to be at least one cell in a row" );

    List<RtfPara> paras = new ArrayList<>();
    for ( Object cell : cells ) {
      if ( cell instanceof RtfPara )
        paras.add( (RtfPara) cell );
      else
        paras.add( p( cell ) );
    }

    RtfPara[] parasArray = new RtfPara[ paras.size() ];
    return rowWithBackgroundColor( colorIndex, paras.toArray( parasArray ) );
  }

  /**
   * Writes a row with a sequence of paragraph cells.
   *
   * @param cells Cells of the row. Must not be {@code null}.
   * @return New object for the row.
   */
  public static RtfRow row( RtfPara... cells ) {
    return rowWithBackgroundColor( 0, cells );
  }

  /**
   * Writes a row with a sequence of paragraph cells.
   *
   * @param colorIndex Row background color.
   * @param cells      Cells of the row. Must not be {@code null}.
   * @return New object for the row.
   */
  public static RtfRow rowWithBackgroundColor( int colorIndex, RtfPara... cells ) {
    /* <row>    := <tbldef> <cell>+ \row
     * <cell>   := <textpar>+ \cell
     *
     * <tbldef> := \trowd \trgaph <rowjust>? & <rowwrite>? & \trleft? \trheader? & \trkeep? <celldef>+
     */
    if ( cells == null )
      throw new RtfException( "There has to be at least one cell in a row" );

    return RtfRow.of( ( self, out, withEndingPar ) -> {
      out.append( "{" ).append( RtfControlWords.ROW_DEFAULTS ).append( RtfControlWords.ROW_AUTOFIT )
         .append( "1" ).append( RtfControlWords.IN_TABLE ).append( '\n' );

      // \cellxN is the cumulative right boundary of the cell in twips, not the cell index.
      int boundary = 0;
      for ( RtfPara cellPara : cells ) {
        int width = (cellPara instanceof RtfTextPara && ((RtfTextPara) cellPara).cellWidthTwips >= 0)
                    ? ((RtfTextPara) cellPara).cellWidthTwips
                    : RtfCell.DEFAULT_CELL_WIDTH_TWIPS;
        boundary += width;
        out.append( self.tbldef )
           .append( (cellPara instanceof RtfTextPara) ?
                    ((RtfTextPara) cellPara).cellfmt :
                    "" )
           .append( RtfControlWords.CELL_BACKGROUND_COLOR ).append( Integer.toString( colorIndex ) )
           .append( RtfControlWords.CELL_BOUNDARY )
           .append( Integer.toString( boundary ) ).append( '\n' );
      }

      for ( RtfPara cell : cells ) {
        cell.rtf( out, false );
        out.append( RtfControlWords.CELL ).append( '\n' );
      }
      out.append( RtfControlWords.ROW ).append( "}" );
    } );
  }

  /**
   * Writes a row from fully specified {@link RtfCell cells}. Unlike the {@code row(...)}
   * convenience methods, each cell carries its own width, background color, borders, vertical
   * alignment and merging. The individual widths are turned into the cumulative {@code \cellx}
   * right-boundaries the RTF format requires; a cell without an explicit width contributes
   * {@link RtfCell#DEFAULT_CELL_WIDTH_TWIPS}.
   *
   * @param cells Cells of the row. Must not be {@code null} or empty.
   * @return New row object.
   */
  public static RtfRow row( RtfCell... cells ) {
    if ( cells == null || cells.length == 0 )
      throw new RtfException( "There has to be at least one cell in a row" );

    return RtfRow.of( ( self, out, withEndingPar ) -> {
      out.append( "{" ).append( RtfControlWords.ROW_DEFAULTS ).append( RtfControlWords.IN_TABLE )
         .append( '\n' );

      int boundary = 0;
      for ( RtfCell cell : cells ) {
        boundary += cell.effectiveWidthTwips();
        out.append( self.tbldef )
           .append( cell.celldef )
           .append( RtfControlWords.CELL_BOUNDARY ).append( Integer.toString( boundary ) )
           .append( '\n' );
      }

      for ( RtfCell cell : cells ) {
        List<RtfPara> paras = cell.paras;
        for ( int i = 0; i < paras.size(); i++ )
          // \par separates paragraphs inside a cell; the final one is terminated by \cell only.
          paras.get( i ).rtf( out, i < paras.size() - 1 );
        out.append( RtfControlWords.CELL ).append( '\n' );
      }

      out.append( RtfControlWords.ROW ).append( "}" );
    } );
  }
}
