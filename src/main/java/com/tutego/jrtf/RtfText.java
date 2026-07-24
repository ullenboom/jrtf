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
import java.io.InputStream;
import java.net.URL;

import org.jspecify.annotations.Nullable;

/**
 * Class for RTF text with different text formattings like bold, italic, ...
 * <p>
 * Many methods here (e.g. {@link #bold(Object)}, {@link #italic(Object)}, {@link #fontSize(int, Object)})
 * and in other classes ({@link RtfPara#p(Object...)}, {@link RtfCell#cell(Object...)},
 * {@link RtfTemplate#inject(String, Object)}) accept a plain {@code Object} instead of requiring a
 * {@code String} or {@link RtfText}, so a caller can pass whatever value is at hand without an
 * explicit conversion. Such an {@code Object} is resolved by its runtime type &mdash; see
 * {@link #text(Object...)} for exactly what happens for each of the four cases (already-formatted
 * {@code RtfText}, a {@link RtfTemplate}, a disallowed {@link RtfPara}, or anything else).
 */
public class RtfText {
  /*
   * <para>    := <textpar> | <row>
   * <textpar> := <pn>? <brdrdef>? <parfmt>* <apoctl>* <tabdef>? <shading>?
   *              (/v /spv)? (\subdocument | <char>+) (\par <para>)?
   *
   * <char>    := <ptext> | <atext> | '{' <char> '}'
   * <ptext>   := (<chrfmt>* <data>+ )+
   * <data>    := #PCDATA | <spec> | <pict> | <obj> |
   *              <do> | <foot> | <annot> | <field> |
   *              <idx> | <toc> | <book>
   */

  /**
   * RTF text. {@code null} exactly when this object was created via {@link #of(Renderer)}.
   */
  private final @Nullable CharSequence rtf;

  /**
   * Render body, used instead of {@link #rtf} when this object was created via {@link #of(Renderer)}.
   * {@code null} unless created via {@link #of(Renderer)}.
   */
  private final @Nullable Renderer renderer;

  /**
   * For wrapping RTF text to this object.
   */
  RtfText( CharSequence rtf ) {
    this.rtf = rtf;
    this.renderer = null;
  }

  private RtfText( Renderer renderer ) {
    this.rtf = null;
    this.renderer = renderer;
  }

  /**
   * Writes the RTF of this RtfText object to the output.
   *
   * @param out Appendable.
   * @throws IOException
   */
  void rtf( Appendable out ) throws IOException {
    if ( renderer != null )
      renderer.rtf( out );
    else
      out.append( rtf );
  }

  /**
   * Functional interface for the render body of a {@code RtfText}, so callers
   * can pass a lambda instead of subclassing {@code RtfText} anonymously.
   */
  @FunctionalInterface
  interface Renderer {
    void rtf( Appendable out ) throws IOException;
  }

  /**
   * Wraps a {@link Renderer} lambda into a {@code RtfText}, evaluated only when
   * the enclosing document is written.
   *
   * @param renderer Render body.
   * @return New {@code RtfText} object delegating to {@code renderer}.
   */
  static RtfText of( Renderer renderer ) {
    return new RtfText( renderer );
  }

  /**
   * Converts every object in the sequence to a {@link RtfText}, then concatenates them.
   * This is the canonical place where this library's "accept a plain {@code Object}" convention
   * (used throughout this class, {@link RtfPara}, {@link RtfCell} and {@link RtfTemplate}) is
   * defined. Each element of {@code texts} is resolved by its runtime type, in this order:
   * <ol>
   *   <li>{@code null} &mdash; the element is skipped entirely (no text, no separating space).</li>
   *   <li>{@link RtfText} &mdash; used verbatim, keeping whatever formatting it already carries
   *       (e.g. the result of {@link #bold(Object)}).</li>
   *   <li>{@link RtfTemplate} &mdash; expanded via {@link RtfTemplate#out()}, i.e. its variable
   *       substitutions are performed and the result is inserted as-is (already RTF-encoded).</li>
   *   <li>{@link RtfPara} &mdash; rejected with a {@link RtfException}. A paragraph has no
   *       sensible {@code toString()} rendering into inline text, so it must be added with
   *       {@link Rtf#section(RtfPara...)} instead of being passed here.</li>
   *   <li>anything else (typically a {@code String}, but really any object) &mdash; converted with
   *       {@code toString()} and RTF-escaped (special characters like {@code {}, }, \} are escaped,
   *       {@code '\n'} becomes a paragraph break, {@code '\t'} becomes a tab).</li>
   * </ol>
   * If the argument is {@code null} or no elements are given the result is equal to {@code text("")}.
   *
   * @param texts Sequence of text. The array itself may be {@code null} (treated like
   *              {@code text("")}) and individual elements may be {@code null} (such an
   *              element is skipped).
   * @return New RtfText object representing this sequence of text.
   */
  public static RtfText text( @Nullable Object @Nullable ... texts ) {
    return textJoinWithSpace( false, texts );
  }

  /**
   * Like {@link #text(Object...)} &mdash; see there for exactly how each element of {@code texts}
   * is resolved &mdash; but additionally joins the elements with a space between them if wanted.
   *
   * @param joinWithSpace If space character should be set between non {@code null} elements.
   * @param texts         Sequence of text. The array itself may be {@code null} (treated like
   *                      {@code text("")}) and individual elements may be {@code null}
   *                      (such an element is skipped).
   * @return New RtfText object representing this sequence of text.
   */
  public static RtfText textJoinWithSpace( boolean joinWithSpace, @Nullable Object @Nullable ... texts ) {
    if ( texts == null || texts.length == 0 )
      return new RtfText( "" );

    StringBuilder result = new StringBuilder( 1024 );
    for ( int i = 0; i < texts.length; i++ ) {
      if ( texts[ i ] == null )
        continue;

      if ( joinWithSpace )
        if ( i > 0 && texts[ i - 1 ] != null )  // if preceding element is null, no space
          result.append( ' ' );

      try {
        if ( texts[ i ] instanceof RtfText )
          ((RtfText) texts[ i ]).rtf( result );
        else if ( texts[ i ] instanceof RtfTemplate )
          result.append( ((RtfTemplate) texts[ i ]).out() );
        else if ( texts[ i ] instanceof RtfPara )  // check more
          throw new RtfException(
              "RtfPara in method text() is not allowed. There is no sensible toString() method declared" );
        else
          Rtf.asRtf( result, texts[ i ].toString() );
      }
      catch ( IOException e ) {
        throw new RtfException( e );
      }
    }
    return new RtfText( result );
  }

  /**
   * Wraps a String in a {@link RtfText} object.
   * If the argument is a {@code null} value then it will be treated like a {@code ""}.
   *
   * @param text String. May be {@code null}, which is treated like {@code ""}.
   * @return New RtfText object representing this text.
   */
  public static RtfText text( @Nullable String text ) {
    if ( text == null )
      text = "";

    return new RtfText( Rtf.asRtf( text ) );
  }

  /**
   * Sets text with a given font style.
   *
   * @param fontnum Font number according to the header.
   * @param text    Text to set with a different font. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText font( int fontnum, Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 8 );
    sb.append( "{" ).append( RtfControlWords.FONT ).append( fontnum )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );

    return new RtfText( sb );
  }

  /**
   * Italic given text.
   *
   * @param text Text to make italic. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText italic( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 5 );
    sb.append( "{" ).append( RtfControlWords.ITALIC ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Bold a given text.
   *
   * @param text Text to be set bold. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this bold text.
   */
  public static RtfText bold( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 5 );
    sb.append( "{" ).append( RtfControlWords.BOLD ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Underline text.
   *
   * @param text Text to underline. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this underlined text.
   */
  public static RtfText underline( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 6 );
    sb.append( "{" ).append( RtfControlWords.UNDERLINE ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Underlines dotted.
   *
   * @param text Text to underline dotted.
   * @return New RtfText object representing this text.
   */
  public static RtfText dottedUnderline( String text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 7 );
    sb.append( "{" ).append( RtfControlWords.UNDERLINE_DOTTED ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Underlines double.
   *
   * @param text Text to underline double. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText doubleUnderline( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 8 );
    sb.append( "{" ).append( RtfControlWords.UNDERLINE_DOUBLE ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Underlines word.
   *
   * @param text Text where words will be underlined. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText wordUnderline( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 7 );
    sb.append( "{" ).append( RtfControlWords.UNDERLINE_WORD ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Subscripts text.
   *
   * @param text Text to subscript. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this subscripted text.
   */
  public static RtfText subscript( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 7 );
    sb.append( "{" ).append( RtfControlWords.SUBSCRIPT ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Set text for revision.
   *
   * @param text Text for revision. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this resivisioned text.
   */
  public static RtfText revised( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 11 );
    sb.append( "{" ).append( RtfControlWords.REVISED ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Superscripts text.
   *
   * @param text Text to superscript. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this superscripted text.
   */
  public static RtfText superscript( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 9 );
    sb.append( "{" ).append( RtfControlWords.SUPERSCRIPT ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Strikes through text.
   *
   * @param text Text to strike through. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this strikes through text.
   */
  public static RtfText strikethru( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 10 );
    sb.append( "{" ).append( RtfControlWords.STRIKETHROUGH ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Shadows text.
   *
   * @param text Text to shadow. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this shadowed text.
   */
  public static RtfText shadow( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 8 );
    sb.append( "{" ).append( RtfControlWords.SHADOW ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Shows text in small capitals.
   *
   * @param text Text to show in small capitals. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText smallCapitals( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 9 );
    sb.append( "{" ).append( RtfControlWords.SMALL_CAPS ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Shows text in all capitals (the underlying characters stay unchanged).
   *
   * @param text Text to show in capitals. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText capitals( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 8 );
    sb.append( "{" ).append( RtfControlWords.CAPS ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Marks text as hidden. Most readers omit hidden text unless explicitly asked to show it.
   *
   * @param text Text to hide. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this hidden text.
   */
  public static RtfText hidden( Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 4 );
    sb.append( "{" ).append( RtfControlWords.HIDDEN ).append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Turns kerning on for text at or above {@code fontSize}.
   *
   * @param fontSize Minimum font size (in half-points) kerning applies to.
   * @param text     Text to kern. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText kerning( int fontSize, Object text ) {
    if ( fontSize < 0 )
      throw new IllegalArgumentException( "Font size can't be negative" );

    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 12 );
    sb.append( "{" ).append( RtfControlWords.KERNING ).append( fontSize )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Expands (positive value) or condenses (negative value) the character spacing.
   *
   * @param twentiethsOfPoint Amount to expand/condense in twentieths of a point.
   * @param text              Text to expand or condense. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText expand( int twentiethsOfPoint, Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 12 );
    sb.append( "{" ).append( RtfControlWords.CHAR_EXPAND ).append( twentiethsOfPoint )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Subscripts text by a given amount, lowering it below the baseline.
   * See {@link #superscriptBy(int, Object)} for the counterpart that raises text.
   *
   * @param halfPoints Amount to lower the text, in half-points.
   * @param text       Text to subscript. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this subscripted text.
   */
  public static RtfText subscriptBy( int halfPoints, Object text ) {
    if ( halfPoints < 0 )
      throw new IllegalArgumentException( "Amount can't be negative" );

    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 8 );
    sb.append( "{" ).append( RtfControlWords.SUBSCRIPT_LOWER ).append( halfPoints )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Superscripts text by a given amount, raising it above the baseline.
   * See {@link #subscriptBy(int, Object)} for the counterpart that lowers text.
   *
   * @param halfPoints Amount to raise the text, in half-points.
   * @param text       Text to superscript. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this superscripted text.
   */
  public static RtfText superscriptBy( int halfPoints, Object text ) {
    if ( halfPoints < 0 )
      throw new IllegalArgumentException( "Amount can't be negative" );

    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 8 );
    sb.append( "{" ).append( RtfControlWords.SUPERSCRIPT_RAISE ).append( halfPoints )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );
    return new RtfText( sb );
  }

  /**
   * Sets the font size in half-points. The default is 24.
   *
   * @param fontSize Font size.
   * @param text     Text to set in a different font size. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText fontSize( int fontSize, Object text ) {
    if ( fontSize < 0 )
      throw new IllegalArgumentException( "Font size can't be negative" );

    RtfText rtfText = text( text );

    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 10 );
    sb.append( "{" ).append( RtfControlWords.FONT_SIZE ).append( fontSize )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );

    return new RtfText( sb );
  }

  /**
   * Sets a background color for the given text.
   *
   * @param colorindex Index of the color set defined in the header.
   * @param text       Text to color. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText backgroundcolor( int colorindex, Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 10 );
    sb.append( "{" ).append( RtfControlWords.CHAR_BACKGROUND_COLOR ).append( colorindex )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );

    return new RtfText( sb );
  }

  /**
   * Sets a background color for the given text.
   *
   * @param colorindex Index of the color set defined in the header.
   * @param text       Text to color.
   * @return New RtfText object representing this text.
   */
  public static RtfText backgroundcolor( int colorindex, String text ) {
    return backgroundcolor( colorindex, text( text ) );
  }

  /**
   * Colors text.
   *
   * @param colorindex Index of the color set defined in the header.
   * @param text       Text to color. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText color( int colorindex, Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 10 );
    sb.append( "{" ).append( RtfControlWords.CHAR_FOREGROUND_COLOR ).append( colorindex )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );

    return new RtfText( sb );
  }

  /**
   * Colors text.
   *
   * @param colorindex Index of the color set defined in the header.
   * @param text       Text to color.
   * @return New RtfText object representing this text.
   */
  public static RtfText color( int colorindex, String text ) {
    return color( colorindex, text( text ) );
  }

  /**
   * Colors text with a foreground and a background color at once.
   *
   * @param foregroundColorIndex Index of the foreground (text) color in the header color table.
   * @param backgroundColorIndex Index of the background color in the header color table.
   * @param text                 Text to color. A {@code null} value is treated like {@code ""}. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText color( int foregroundColorIndex, int backgroundColorIndex, Object text ) {
    RtfText rtfText = text( text );
    StringBuilder sb = new StringBuilder( rtfText.rtf.length() + 16 );
    sb.append( "{" ).append( RtfControlWords.CHAR_FOREGROUND_COLOR ).append( foregroundColorIndex )
      .append( RtfControlWords.CHAR_BACKGROUND_COLOR ).append( backgroundColorIndex )
      .append( ' ' ).append( rtfText.rtf ).append( '}' );

    return new RtfText( sb );
  }

  // Special Characters
  // <spec>

  /**
   * Current date. Useful in headers.
   *
   * @return New RtfText object representing this current date.
   */
  public static RtfText currentDate() {
    return new RtfText( RtfControlWords.CURRENT_DATE + "\n" );
  }

  /**
   * Current date in long format. Useful in headers
   *
   * @return New RtfText object representing this current date.
   */
  public static RtfText currentDateLong() {
    return new RtfText( RtfControlWords.CURRENT_DATE_LONG + "\n" );
  }

  /**
   * Current date in abbreviated format. Useful in headers.
   *
   * @return New RtfText object representing this current date.
   */
  public static RtfText currentDateAbbreviated() {
    return new RtfText( RtfControlWords.CURRENT_DATE_ABBREVIATED + "\n" );
  }

  /**
   * Current time. Useful in headers.
   *
   * @return New RtfText object representing current time.
   */
  public static RtfText currentTime() {
    return new RtfText( RtfControlWords.CURRENT_TIME + "\n" );
  }

  /**
   * Current page number. Useful in headers.
   *
   * @return New RtfText object representing page number.
   */
  public static RtfText currentPageNumber() {
    return new RtfText( RtfControlWords.CURRENT_PAGE_NUMBER + "\n" );
  }

  /**
   * Current section number. Useful in headers.
   *
   * @return New RtfText object representing the section number.
   */
  public static RtfText currentSectionNumber() {
    return new RtfText( RtfControlWords.CURRENT_SECTION_NUMBER + "\n" );
  }

  /**
   * Required page break.
   *
   * @return New RtfText object representing a page break.
   */
  public static RtfText pageBreak() {
    return new RtfText( RtfControlWords.PAGE_BREAK + "\n" );
  }

  /**
   * Required column break.
   *
   * @return New RtfText object representing a column break.
   */
  public static RtfText columnBreak() {
    return new RtfText( RtfControlWords.COLUMN_BREAK + "\n" );
  }

  /**
   * Required line break (no paragraph break).
   *
   * @return New RtfText object representing a like break.
   */
  public static RtfText lineBreak() {
    return new RtfText( RtfControlWords.LINE_BREAK + "\n" );
  }

  /**
   * Non-required page break. Emitted as it appears in galley view.
   *
   * @return New RtfText object representing a soft page break.
   */
  public static RtfText softPageBreak() {
    return new RtfText( RtfControlWords.SOFT_PAGE_BREAK + "\n" );
  }

  /**
   * Horizontal rule drawn as a bottom paragraph border.
   *
   * @return New RtfText object representing the rule.
   */
  public static RtfText hardRule( double lineWidth, RtfUnit lineWidthUnit ) {
    int lineWidthTwips = lineWidthUnit.toTwips( lineWidth );
    // customize the line color with \dplineco[rgb], width with \dplinew
    // return new RtfText( String.format("{\\pard {\\*\\do\\dobxcolumn\\dobypara\\dodhgt" +
    //                                   "\\dpline\\dpxsize9200\\dplinesolid\\dplinew%d} \\par}",
    //                                   lineWidthTwips ));
    return new RtfText( String.format(
        RtfControlWords.BOTTOM_BORDER + RtfControlWords.BORDER_SINGLE + RtfControlWords.BORDER_WIDTH + "%d {" +
        RtfControlWords.FONT_SIZE + "0" + RtfControlWords.NON_BREAKING_SPACE + "}",
        lineWidthTwips ) );
  }

  /**
   * Non-required column break. Emitted as it appears in galley view.
   *
   * @return New RtfText object representing a column break.
   */
  public static RtfText softColumnBreak() {
    return new RtfText( RtfControlWords.SOFT_COLUMN_BREAK + "\n" );
  }

  /**
   * Non-required line break. Emitted as it appears in galley view.
   *
   * @return New RtfText object representing a soft line break.
   */
  public static RtfText softLineBreak() {
    return new RtfText( RtfControlWords.SOFT_LINE_BREAK + "\n" );
  }

  /**
   * Tab character. You can also insert a regular "\t" in text.
   * See {@link RtfTextPara#tab(double, RtfUnit)} for adjustments.
   *
   * @return New RtfText object representing a tab.
   */
  public static RtfText tab() {
    return new RtfText( RtfControlWords.TAB + "\n" );
  }

  /**
   * Em-dash (long hyphen).
   *
   * @return New RtfText object representing a hyphen.
   */
  public static RtfText longHyphen() {
    return new RtfText( RtfControlWords.EM_DASH + "\n" );
  }

  /**
   * En-dash (short hyphen).
   *
   * @return New RtfText object representing a hypen.
   */
  public static RtfText shortHyphen() {
    return new RtfText( RtfControlWords.EN_DASH + "\n" );
  }

  /**
   * Bullet character.
   *
   * @return New RtfText object representing a bullet.
   */
  public static RtfText bullet() {
    return new RtfText( RtfControlWords.BULLET + "\n" );
  }

  /**
   * Left single quotation mark.
   *
   * @return New RtfText object representing this text.
   */
  public static RtfText leftQuotationMark() {
    return new RtfText( RtfControlWords.LEFT_SINGLE_QUOTE + "\n" );
  }

  /**
   * Right single quotation mark.
   *
   * @return New RtfText object representing this text.
   */
  public static RtfText rightQuotationMark() {
    return new RtfText( RtfControlWords.RIGHT_SINGLE_QUOTE + "\n" );
  }

  /**
   * Sets a text in single quotation marks.
   *
   * @param text Text to put in quotes.
   * @return New RtfText object representing this text in quotation marks.
   */
  public static RtfText qoute( Object text ) {
    RtfText rtfText = text( text );
    return new RtfText( RtfControlWords.LEFT_SINGLE_QUOTE + "\n" + rtfText.rtf +
                        RtfControlWords.RIGHT_SINGLE_QUOTE + "\n" );
  }

  /**
   * Left double quotation mark.
   *
   * @return New RtfText object representing this text.
   */
  public static RtfText leftDoubleQuotationMark() {
    return new RtfText( RtfControlWords.LEFT_DOUBLE_QUOTE + "\n" );
  }

  /**
   * Right double quotation mark.
   *
   * @return New RtfText object representing this text.
   */
  public static RtfText rightDoubleQuotationMark() {
    return new RtfText( RtfControlWords.RIGHT_DOUBLE_QUOTE + "\n" );
  }

  /**
   * Sets a text in double quotation marks.
   *
   * @param text to put in quotes.
   * @return New RtfText object representing this text in quotation marks.
   */
  public static RtfText doubleQuote( Object text ) {
    RtfText rtfText = text( text );
    return new RtfText( RtfControlWords.LEFT_DOUBLE_QUOTE + "\n" + rtfText.rtf +
                        RtfControlWords.RIGHT_DOUBLE_QUOTE + "\n" );
  }

  /**
   * Non-breaking space.
   *
   * @return New RtfText object representing this text.
   */
  public static RtfText nonBreakingSpace() {
    return new RtfText( RtfControlWords.NON_BREAKING_SPACE + "\n" );
  }

  // <pict>

  /**
   * Place a picture. The URL is only opened and read when the enclosing document
   * is actually written (i.e. on {@code out()}), not when this method is called.
   *
   * @param source URL of the image. Must not be {@code null}.
   * @return New {@link RtfPicture} object.
   */
  public static RtfPicture picture( URL source ) {
    if ( source == null )
      throw new IllegalArgumentException( "Image source can't be null" );

    return new RtfPicture( source::openStream );
  }

  /**
   * Place a picture. The stream is only read when the enclosing document is
   * actually written (i.e. on {@code out()}), not when this method is called.
   *
   * @param source InputStream of the image. Must not be {@code null}.
   * @return New {@link RtfPicture} object.
   */
  public static RtfPicture picture( InputStream source ) {
    if ( source == null )
      throw new IllegalArgumentException( "Image source can't be null" );

    return new RtfPicture( () -> source );
  }

  // <foot>  '{' \footnote <para>+ '}'

  /**
   * Place a footnote with automatic footnote reference.
   *
   * @param paras Paragraphs of this footnote.
   * @return New RtfText object representing this footnote.
   */
  public static RtfText footnote( RtfPara... paras ) {
    return RtfText.of( out -> {
      out.append( RtfControlWords.FOOTNOTE_REF_MARK ).append( "{" ).append( RtfControlWords.FOOTNOTE_DESTINATION )
         .append( "{" ).append( RtfControlWords.SUPERSCRIPT_RAISE ).append( "6" )
         .append( RtfControlWords.FOOTNOTE_REF_MARK ).append( " }" );
      for ( RtfPara rtfPara : paras )
        rtfPara.rtf( out, false );
      out.append( "}\n" );
    } );
  }

  /**
   * Place a footnote with automatic footnote reference.
   *
   * @param para Paragraph of this footnote.
   * @return New RtfText object representing this footnote.
   */
  public static RtfText footnote( Object para ) {
    return footnote( new RtfPara[]{ RtfPara.p( para ) } );
  }

  // Fields  

  /**
   * Modifiers for fields.
   */
  public enum FieldModifier {
    /**
     * A formatting change has been made to the field result since the field was last updated.
     */
    DIRTY {
      @Override public String toString() {return RtfControlWords.FIELD_DIRTY;}
    },

    /**
     * Text has been added to, or removed from, the field result since the field was last updated.
     */
    EDITED {
      @Override public String toString() {return RtfControlWords.FIELD_EDIT;}
    },

    /**
     * Field is locked and cannot be updated.
     */
    LOCKED {
      @Override public String toString() {return RtfControlWords.FIELD_LOCKED;}
    },

    /**
     * Result is not in a form suitable for display (for example, binary data
     * used by fields whose result is a picture).
     */
    NONDISPLAYABLE {
      @Override public String toString() {return RtfControlWords.FIELD_PRIVATE;}
    }
  }

  /**
   * Inserts an RTF field.
   *
   * @param fieldInstructions Field instructions. Must not be {@code null}.
   * @param recentResult      Recent results of this field. May be {@code null} (no result is written).
   * @param fieldModifier     Additional field modifier. May be {@code null} (no modifier is written).
   * @return New RtfText object representing this field.
   */
  public static RtfText field( RtfPara fieldInstructions, @Nullable RtfPara recentResult,
                               final @Nullable FieldModifier fieldModifier ) {
    if ( fieldInstructions == null )
      throw new IllegalArgumentException( "Field instructions are missing" );

    /*
     * <field>     := '{' \field <fieldmod>? <fieldinst> <fieldrslt> '}'
     * <fieldmod>  := \flddirty? & \fldedit? & \fldlock? & \fldpriv?
     * <fieldinst> := '{\*' \fldinst <para>+ <fldalt>? '}'
     * <fldalt>    := \fldalt
     * <fieldrslt> := '{' \fldrslt <para>+ '}'
     */
    return RtfText.of( out -> {
      out.append( "{" ).append( RtfControlWords.FIELD );

      if ( fieldModifier != null )
        out.append( fieldModifier.toString() );

      out.append( "{" ).append( RtfControlWords.FIELD_INSTRUCTION_DESTINATION ).append( ' ' );
      fieldInstructions.rtf( out, false );
      out.append( "}{" ).append( RtfControlWords.FIELD_RESULT_DESTINATION ).append( ' ' );

      if ( recentResult != null )
        recentResult.rtf( out, false );

      out.append( "}}" );
    } );
  }

  /**
   * Inserts a RTF field.
   *
   * @param fieldInstructions Field instructions. Must not be {@code null}.
   * @param recentResult      Recent results of this field. May be {@code null} (no result is written).
   * @return New RtfText object representing this field.
   */
  public static RtfText field( RtfPara fieldInstructions, @Nullable RtfPara recentResult ) {
    return field( fieldInstructions, recentResult, null );
  }

  /**
   * Adds an hyperlink (aka anchor). This is a special field.
   *
   * @param url  URL of this hyperlink.
   * @param text Text for this hyperlink.
   * @return New RtfText object representing this hyperlink.
   */
  public static RtfText hyperlink( String url, RtfPara text ) {
    return RtfText.of( out -> {
      out.append( "{" ).append( RtfControlWords.FIELD )
         .append( "{" ).append( RtfControlWords.FIELD_INSTRUCTION_DESTINATION ).append( "{HYPERLINK \"" )
         .append( Rtf.asRtf( url ) )
         .append( "\"}}{" ).append( RtfControlWords.FIELD_RESULT_DESTINATION )
         .append( "{" ).append( RtfControlWords.UNDERLINE ).append( ' ' );
      text.rtf( out, false );
      out.append( "}}}" );
    } );
  }

  /**
   * Adds a hyperlink to a {@link #bookmark(String, Object...) bookmark} elsewhere in this
   * document, instead of to an external URL.
   *
   * @param bookmarkName Name of the target bookmark, as passed to {@link #bookmark(String, Object...)}.
   * @param text         Text of the hyperlink.
   * @return New RtfText object representing this hyperlink.
   */
  public static RtfText hyperlinkToBookmark( String bookmarkName, RtfPara text ) {
    return RtfText.of( out -> {
      out.append( "{" ).append( RtfControlWords.FIELD )
         .append( "{" ).append( RtfControlWords.FIELD_INSTRUCTION_DESTINATION ).append( "{HYPERLINK " )
         .append( RtfControlWords.FIELD_SWITCH_HYPERLINK_BOOKMARK ).append( " \"" )
         .append( Rtf.asRtf( bookmarkName ) )
         .append( "\"}}{" ).append( RtfControlWords.FIELD_RESULT_DESTINATION )
         .append( "{" ).append( RtfControlWords.UNDERLINE ).append( ' ' );
      text.rtf( out, false );
      out.append( "}}}" );
    } );
  }

  // <bookmark>  '{' \bkmkstart <text> '}' ... '{' \bkmkend <text> '}'

  /**
   * Marks the given content as a bookmark, so it can be jumped to with
   * {@link #hyperlinkToBookmark(String, RtfPara)}. Every bookmark name used in a document
   * should be unique.
   *
   * @param name  Name of the bookmark. Plain ASCII, no spaces or braces. Must not be {@code null}.
   * @param texts Content to mark. Converted like {@link #text(Object...)}.
   * @return New RtfText object representing the bookmarked content.
   */
  public static RtfText bookmark( String name, @Nullable Object @Nullable ... texts ) {
    if ( name == null )
      throw new IllegalArgumentException( "Bookmark name can't be null" );

    RtfText content = text( texts );
    return RtfText.of( out -> {
      out.append( "{" ).append( RtfControlWords.BOOKMARK_START ).append( ' ' ).append( name ).append( "}" );
      content.rtf( out );
      out.append( "{" ).append( RtfControlWords.BOOKMARK_END ).append( ' ' ).append( name ).append( "}" );
    } );
  }
}
