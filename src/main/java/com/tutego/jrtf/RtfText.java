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

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

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
   * Renders RTF content lazily — only evaluated when the enclosing document is written.
   */
  private final Consumer<RtfOutput> renderer;

  /**
   * Wraps a lazy RTF renderer.
   */
  RtfText( Consumer<RtfOutput> renderer ) {
    this.renderer = renderer;
  }

  /**
   * Writes the RTF of this RtfText object to the output.
   *
   * @param out Output buffer.
   */
  void rtf( RtfOutput out ) {
    renderer.accept( out );
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
   *       {@code toString()} and RTF-escaped (special characters like <code>{</code>, <code>}</code>,
 *       and <code>\</code> are escaped),
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
      return new RtfText( out -> {} );

    return new RtfText( out -> {
      for ( int i = 0; i < texts.length; i++ ) {
        if ( texts[ i ] == null )
          continue;

        if ( joinWithSpace
             && i > 0 && texts[ i - 1 ] != null )  // if preceding element is null, no space
          out.sp();

        if ( texts[ i ] instanceof RtfText )
          ((RtfText) texts[ i ]).rtf( out );
        else if ( texts[ i ] instanceof RtfTemplate )
          out.append( ((RtfTemplate) texts[ i ]).out() );
        else if ( texts[ i ] instanceof RtfPara )  // check more
          throw new RtfException(
              "RtfPara in method text() is not allowed. There is no sensible toString() method declared" );
        else
          Rtf.asRtf( out, texts[ i ].toString() );
      }
    } );
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

    String t = text;
    return new RtfText( out -> Rtf.asRtf( out, t ) );
  }

  // -- Language ----------------------------------------------------------------

  static final Map<Locale, Integer> LOCALE_TO_LCID = new HashMap<>();

  static {
    // German
    LOCALE_TO_LCID.put( Locale.GERMAN,              1031 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "de-AT" ), 3079 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "de-CH" ), 2055 );
    // English
    LOCALE_TO_LCID.put( Locale.ENGLISH,              1033 );
    LOCALE_TO_LCID.put( Locale.UK,                   2057 );
    LOCALE_TO_LCID.put( Locale.US,                   1033 );
    // French
    LOCALE_TO_LCID.put( Locale.FRENCH,               1036 );
    LOCALE_TO_LCID.put( Locale.CANADA_FRENCH,        3084 );
    // Italian
    LOCALE_TO_LCID.put( Locale.ITALIAN,              1040 );
    // Spanish
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "es" ),    1034 );
    // Portuguese
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "pt" ),    2070 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "pt-BR" ), 1046 );
    // Dutch
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "nl" ),    1043 );
    // Russian
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "ru" ),    1049 );
    // Japanese
    LOCALE_TO_LCID.put( Locale.JAPANESE,             1041 );
    // Chinese
    LOCALE_TO_LCID.put( Locale.CHINESE,              2052 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "zh-TW" ), 1028 );
    // Korean
    LOCALE_TO_LCID.put( Locale.KOREAN,               1042 );
    // Other common locales
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "ar" ),    1025 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "tr" ),    1055 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "pl" ),    1045 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "sv" ),    1053 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "no" ),    1044 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "da" ),    1030 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "fi" ),    1035 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "cs" ),    1029 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "hu" ),    1038 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "ro" ),    1048 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "el" ),    1032 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "he" ),    1037 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "th" ),    1054 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "vi" ),    1066 );
    LOCALE_TO_LCID.put( Locale.forLanguageTag( "hi" ),    1081 );
  }

  /**
   * Sets the language for the given text using a {@link Locale}, which is
   * much more common in the Java world than a Windows LCID. The locale is
   * mapped to the closest matching LCID via a built-in table covering
   * ~30 languages. For locales not in the table, provide the LCID directly
   * via {@link #language(int, Object)}.
   *
   * @param locale Language to use for spell checking and hyphenation.
   * @param text   Text to associate with this language.
   * @return New RtfText object.
   */
  public static RtfText language( Locale locale, Object text ) {
    Integer lcid = LOCALE_TO_LCID.get( locale );
    if ( lcid == null )
      throw new IllegalArgumentException(
          "No LCID mapping for " + locale + ". Use language(int lcid, Object text) "
          + "with the numeric Windows Language Code Identifier." );
    return language( lcid, text );
  }

  /**
   * Sets the language for the given text, used by word processors for
   * spell checking and hyphenation. Common LCIDs: 1031 (German),
   * 1033 (US English), 1036 (French), 1040 (Italian).
   * Prefer {@link #language(Locale, Object)} when possible.
   *
   * @param lcid Windows Language Code Identifier.
   * @param text Text to associate with this language.
   * @return New RtfText object.
   */
  public static RtfText language( int lcid, Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.LANGUAGE, lcid ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Embossed text (raised appearance, light on top).
   *
   * @param text Text to emboss.
   * @return New RtfText object.
   */
  public static RtfText emboss( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.EMBOSS ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Engraved text (sunken appearance, shadow on top).
   *
   * @param text Text to engrave.
   * @return New RtfText object.
   */
  public static RtfText engrave( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.ENGRAVE ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Outline text (hollow characters, only the outline is drawn).
   *
   * @param text Text to render in outline.
   * @return New RtfText object.
   */
  public static RtfText outline( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.OUTLINE ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Sets text with a given font style.
   *
   * @param fontnum Font number according to the header.
   * @param text    Text to set with a different font. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText font( int fontnum, Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.FONT ).append( fontnum ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Italic given text.
   *
   * @param text Text to make italic. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText italic( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.ITALIC ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Bold a given text.
   *
   * @param text Text to be set bold. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this bold text.
   */
  public static RtfText bold( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.BOLD ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Underline text.
   *
   * @param text Text to underline. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this underlined text.
   */
  public static RtfText underline( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.UNDERLINE ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Underlines dotted.
   *
   * @param text Text to underline dotted.
   * @return New RtfText object representing this text.
   */
  public static RtfText dottedUnderline( String text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.UNDERLINE_DOTTED ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Underlines double.
   *
   * @param text Text to underline double. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText doubleUnderline( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.UNDERLINE_DOUBLE ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Underlines word.
   *
   * @param text Text where words will be underlined. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText wordUnderline( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.UNDERLINE_WORD ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Subscripts text.
   *
   * @param text Text to subscript. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this subscripted text.
   */
  public static RtfText subscript( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.SUBSCRIPT ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Set text for revision.
   *
   * @param text Text for revision. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this resivisioned text.
   */
  public static RtfText revised( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.REVISED ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Superscripts text.
   *
   * @param text Text to superscript. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this superscripted text.
   */
  public static RtfText superscript( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.SUPERSCRIPT ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Strikes through text.
   *
   * @param text Text to strike through. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this strikes through text.
   */
  public static RtfText strikethru( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.STRIKETHROUGH ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Shadows text.
   *
   * @param text Text to shadow. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this shadowed text.
   */
  public static RtfText shadow( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.SHADOW ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Shows text in small capitals.
   *
   * @param text Text to show in small capitals. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText smallCapitals( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.SMALL_CAPS ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Shows text in all capitals (the underlying characters stay unchanged).
   *
   * @param text Text to show in capitals. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText capitals( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.CAPS ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Marks text as hidden. Most readers omit hidden text unless explicitly asked to show it.
   *
   * @param text Text to hide. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this hidden text.
   */
  public static RtfText hidden( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.HIDDEN ).sp();
      inner.rtf( out );
      out.close();
    } );
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

    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.KERNING, fontSize ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Expands (positive value) or condenses (negative value) the character spacing.
   *
   * @param twentiethsOfPoint Amount to expand/condense in twentieths of a point.
   * @param text              Text to expand or condense. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText expand( int twentiethsOfPoint, Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.CHAR_EXPAND, twentiethsOfPoint ).sp();
      inner.rtf( out );
      out.close();
    } );
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

    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.SUBSCRIPT_LOWER, halfPoints ).sp();
      inner.rtf( out );
      out.close();
    } );
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

    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.SUPERSCRIPT_RAISE, halfPoints ).sp();
      inner.rtf( out );
      out.close();
    } );
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

    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.FONT_SIZE, fontSize ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Sets a background color for the given text.
   *
   * @param colorindex Index of the color set defined in the header.
   * @param text       Text to color. See {@link #text(Object...)} for how {@code text} is resolved.
   * @return New RtfText object representing this text.
   */
  public static RtfText backgroundcolor( int colorindex, Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.CHAR_BACKGROUND_COLOR, colorindex ).sp();
      inner.rtf( out );
      out.close();
    } );
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
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.CHAR_FOREGROUND_COLOR, colorindex ).sp();
      inner.rtf( out );
      out.close();
    } );
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
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open()
         .cw( RtfControlWords.CHAR_FOREGROUND_COLOR, foregroundColorIndex )
         .cw( RtfControlWords.CHAR_BACKGROUND_COLOR, backgroundColorIndex )
         .sp();
      inner.rtf( out );
      out.close();
    } );
  }

  // Special Characters
  // <spec>

  /**
   * Current date. Useful in headers.
   *
   * @return New RtfText object representing this current date.
   */
  public static RtfText currentDate() {
    return new RtfText( out -> out.ctrl( RtfControlWords.CURRENT_DATE ) );
  }

  /**
   * Current date in long format. Useful in headers
   *
   * @return New RtfText object representing this current date.
   */
  public static RtfText currentDateLong() {
    return new RtfText( out -> out.ctrl( RtfControlWords.CURRENT_DATE_LONG ) );
  }

  /**
   * Current date in abbreviated format. Useful in headers.
   *
   * @return New RtfText object representing this current date.
   */
  public static RtfText currentDateAbbreviated() {
    return new RtfText( out -> out.ctrl( RtfControlWords.CURRENT_DATE_ABBREVIATED ) );
  }

  /**
   * Current time. Useful in headers.
   *
   * @return New RtfText object representing current time.
   */
  public static RtfText currentTime() {
    return new RtfText( out -> out.ctrl( RtfControlWords.CURRENT_TIME ) );
  }

  /**
   * Current page number. Useful in headers.
   *
   * @return New RtfText object representing page number.
   */
  public static RtfText currentPageNumber() {
    return new RtfText( out -> out.ctrl( RtfControlWords.CURRENT_PAGE_NUMBER ) );
  }

  /**
   * Current section number. Useful in headers.
   *
   * @return New RtfText object representing the section number.
   */
  public static RtfText currentSectionNumber() {
    return new RtfText( out -> out.ctrl( RtfControlWords.CURRENT_SECTION_NUMBER ) );
  }

  /**
   * Required page break.
   *
   * @return New RtfText object representing a page break.
   */
  public static RtfText pageBreak() {
    return new RtfText( out -> out.ctrl( RtfControlWords.PAGE_BREAK ) );
  }

  /**
   * Required column break.
   *
   * @return New RtfText object representing a column break.
   */
  public static RtfText columnBreak() {
    return new RtfText( out -> out.ctrl( RtfControlWords.COLUMN_BREAK ) );
  }

  /**
   * Required line break (no paragraph break).
   *
   * @return New RtfText object representing a like break.
   */
  public static RtfText lineBreak() {
    return new RtfText( out -> out.ctrl( RtfControlWords.LINE_BREAK ) );
  }

  /**
   * Non-required page break. Emitted as it appears in galley view.
   *
   * @return New RtfText object representing a soft page break.
   */
  public static RtfText softPageBreak() {
    return new RtfText( out -> out.ctrl( RtfControlWords.SOFT_PAGE_BREAK ) );
  }

  /**
   * Horizontal rule drawn as a bottom paragraph border.
   *
   * @return New RtfText object representing the rule.
   */
  public static RtfText hardRule( double lineWidth, RtfUnit lineWidthUnit ) {
    int lineWidthTwips = lineWidthUnit.toTwips( lineWidth );
    return new RtfText( out -> {
      out.cw( RtfControlWords.BOTTOM_BORDER ).cw( RtfControlWords.BORDER_SINGLE )
         .cw( RtfControlWords.BORDER_WIDTH ).append( lineWidthTwips )
         .open( RtfControlWords.FONT_SIZE ).append( "0" )
         .cw( RtfControlWords.NON_BREAKING_SPACE ).close();
    } );
  }

  /**
   * Non-required column break. Emitted as it appears in galley view.
   *
   * @return New RtfText object representing a column break.
   */
  public static RtfText softColumnBreak() {
    return new RtfText( out -> out.ctrl( RtfControlWords.SOFT_COLUMN_BREAK ) );
  }

  /**
   * Non-required line break. Emitted as it appears in galley view.
   *
   * @return New RtfText object representing a soft line break.
   */
  public static RtfText softLineBreak() {
    return new RtfText( out -> out.ctrl( RtfControlWords.SOFT_LINE_BREAK ) );
  }

  /**
   * Tab character. You can also insert a regular "\t" in text.
   * See {@link RtfTextPara#tab(double, RtfUnit)} for adjustments.
   *
   * @return New RtfText object representing a tab.
   */
  public static RtfText tab() {
    return new RtfText( out -> out.ctrl( RtfControlWords.TAB ) );
  }

  /**
   * Em-dash (long hyphen).
   *
   * @return New RtfText object representing a hyphen.
   */
  public static RtfText longHyphen() {
    return new RtfText( out -> out.ctrl( RtfControlWords.EM_DASH ) );
  }

  /**
   * En-dash (short hyphen).
   *
   * @return New RtfText object representing a hypen.
   */
  public static RtfText shortHyphen() {
    return new RtfText( out -> out.ctrl( RtfControlWords.EN_DASH ) );
  }

  /**
   * Bullet character.
   *
   * @return New RtfText object representing a bullet.
   */
  public static RtfText bullet() {
    return new RtfText( out -> out.ctrl( RtfControlWords.BULLET ) );
  }

  /**
   * Left single quotation mark.
   *
   * @return New RtfText object representing this text.
   */
  public static RtfText leftQuotationMark() {
    return new RtfText( out -> out.ctrl( RtfControlWords.LEFT_SINGLE_QUOTE ) );
  }

  /**
   * Right single quotation mark.
   *
   * @return New RtfText object representing this text.
   */
  public static RtfText rightQuotationMark() {
    return new RtfText( out -> out.ctrl( RtfControlWords.RIGHT_SINGLE_QUOTE ) );
  }

  /**
   * Sets a text in single quotation marks.
   *
   * @param text Text to put in quotes.
   * @return New RtfText object representing this text in quotation marks.
   */
  public static RtfText qoute( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.ctrl( RtfControlWords.LEFT_SINGLE_QUOTE );
      inner.rtf( out );
      out.ctrl( RtfControlWords.RIGHT_SINGLE_QUOTE );
    } );
  }

  /**
   * Left double quotation mark.
   *
   * @return New RtfText object representing this text.
   */
  public static RtfText leftDoubleQuotationMark() {
    return new RtfText( out -> out.ctrl( RtfControlWords.LEFT_DOUBLE_QUOTE ) );
  }

  /**
   * Right double quotation mark.
   *
   * @return New RtfText object representing this text.
   */
  public static RtfText rightDoubleQuotationMark() {
    return new RtfText( out -> out.ctrl( RtfControlWords.RIGHT_DOUBLE_QUOTE ) );
  }

  /**
   * Sets a text in double quotation marks.
   *
   * @param text to put in quotes.
   * @return New RtfText object representing this text in quotation marks.
   */
  public static RtfText doubleQuote( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.ctrl( RtfControlWords.LEFT_DOUBLE_QUOTE );
      inner.rtf( out );
      out.ctrl( RtfControlWords.RIGHT_DOUBLE_QUOTE );
    } );
  }

  /**
   * Non-breaking space.
   *
   * @return New RtfText object representing this text.
   */
  public static RtfText nonBreakingSpace() {
    return new RtfText( out -> out.ctrl( RtfControlWords.NON_BREAKING_SPACE ) );
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
    return new RtfText( out -> {
      out.cw( RtfControlWords.FOOTNOTE_REF_MARK ).open( RtfControlWords.FOOTNOTE_DESTINATION )
         .open( RtfControlWords.SUPERSCRIPT_RAISE ).append( "6" )
         .cw( RtfControlWords.FOOTNOTE_REF_MARK ).append( " }" );
      for ( RtfPara rtfPara : paras )
        rtfPara.rtf( out, false );
      out.close().nl();
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
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.FIELD );

      if ( fieldModifier != null )
        out.cw( fieldModifier.toString() );

      out.open().cw( RtfControlWords.FIELD_INSTRUCTION_DESTINATION ).sp();
      fieldInstructions.rtf( out, false );
      out.close().open( RtfControlWords.FIELD_RESULT_DESTINATION ).sp();

      if ( recentResult != null )
        recentResult.rtf( out, false );

      out.close().close();
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
    if ( url == null )
      throw new IllegalArgumentException( "URL must not be null" );
    if ( text == null )
      throw new IllegalArgumentException( "Hyperlink text must not be null" );
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.FIELD )
         .open().cw( RtfControlWords.FIELD_INSTRUCTION_DESTINATION ).append( "{HYPERLINK \"" )
         .append( Rtf.asRtf( url ) )
         .append( "\"}" ).close().open( RtfControlWords.FIELD_RESULT_DESTINATION )
         .open( RtfControlWords.UNDERLINE ).sp();
      text.rtf( out, false );
      out.close().close().close();
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
    if ( bookmarkName == null )
      throw new IllegalArgumentException( "Bookmark name must not be null" );
    if ( text == null )
      throw new IllegalArgumentException( "Hyperlink text must not be null" );
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.FIELD )
         .open().cw( RtfControlWords.FIELD_INSTRUCTION_DESTINATION ).append( "{HYPERLINK " )
         .append( RtfFields.FIELD_SWITCH_HYPERLINK_BOOKMARK ).append( " \"" )
         .append( Rtf.asRtf( bookmarkName ) )
         .append( "\"}" ).close().open( RtfControlWords.FIELD_RESULT_DESTINATION )
         .open( RtfControlWords.UNDERLINE ).sp();
      text.rtf( out, false );
      out.close().close().close();
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
    return new RtfText( out -> {
      out.tag( RtfControlWords.BOOKMARK_START, " " + name );
      content.rtf( out );
      out.tag( RtfControlWords.BOOKMARK_END, " " + name );
    } );
  }

  // RtfField — generic field support

  /**
   * Inserts a {@link RtfField}, which encapsulates a field instruction and
   * its optional recent result.
   *
   * @param rtfField Field to insert.
   * @return New RtfText object representing this field.
   */
  public static RtfText field( RtfField rtfField ) {
    return new RtfText( out -> {
      out.open().cw( RtfControlWords.FIELD );

      if ( rtfField.modifier != null )
        out.cw( rtfField.modifier.toString() );

      out.open().cw( RtfControlWords.FIELD_INSTRUCTION_DESTINATION ).sp();
      out.append( rtfField.instruction );
      out.close();

      if ( rtfField.recentResult != null ) {
        out.open( RtfControlWords.FIELD_RESULT_DESTINATION ).sp();
        rtfField.recentResult.rtf( out, false );
        out.close();
      }

      out.close();
    } );
  }

  // Form fields

  /**
   * Inserts a form field (text input, checkbox, or dropdown).
   *
   * @param formField Form field built with {@link RtfFormField#text()},
   *                  {@link RtfFormField#checkbox()}, or {@link RtfFormField#dropdown()}.
   * @return New RtfText object representing this form field.
   */
  public static RtfText formField( RtfFormField formField ) {
    return new RtfText( formField::rtf );
  }

  // Annotations / comments

  /**
   * Inserts an annotation (comment) with an author and content paragraphs.
   * The word processor shows the author initials as a reference mark,
   * with the comment text in a balloon or review pane.
   *
   * @param author Author initials (e.g. "CU"). Must not be {@code null}.
   * @param paras  Paragraphs forming the comment body.
   * @return New RtfText object representing this comment.
   */
  public static RtfText comment( String author, RtfPara... paras ) {
    if ( author == null || author.isEmpty() )
      throw new IllegalArgumentException( "Comment author is missing" );

    return new RtfText( out -> {
      out.open().cw( RtfControlWords.ANNOTATION_ID ).sp()   // just a placeholder marker
         .cw( RtfControlWords.ANNOTATION_AUTHOR ).sp()
         .append( Rtf.asRtf( author ) ).close();
      out.open( RtfControlWords.ANNOTATION_DESTINATION );
      for ( RtfPara para : paras )
        para.rtf( out, false );
      out.close();
    } );
  }

  // Track changes / revision

  /**
   * Marks text as revised (tracked change). The original text is shown as deleted
   * and the revised text as inserted. The enclosing word processor displays the
   * change with the given author and date.
   *
   * @param author   Author initials (e.g. "CU"). May be {@code null} (no author info).
   * @param original Original text before the change (shown as deleted).
   * @param revised  Revised text after the change (shown as inserted).
   * @return New RtfText object representing this tracked change.
   */
  public static RtfText revision( @Nullable String author, Object original, Object revised ) {
    return new RtfText( out -> {
      if ( author != null && !author.isEmpty() ) {
        out.cw( RtfControlWords.REVISION_AUTHOR ).sp()
           .append( Rtf.asRtf( author ) );
      }
      // deleted original
      out.open( RtfControlWords.REVISED ).sp();
      RtfText.text( original ).rtf( out );
      out.close();
      // inserted revision
      RtfText.text( revised ).rtf( out );
    } );
  }

  // Bidirectional text

  /**
   * Marks text as right-to-left (e.g. for Arabic or Hebrew). Use
   * {@link #leftToRight(Object)} to embed LTR runs inside RTL paragraphs.
   *
   * @param text Text in a right-to-left script.
   * @return New RtfText object with RTL direction.
   */
  public static RtfText rightToLeft( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.RIGHT_TO_LEFT_CHAR ).sp();
      inner.rtf( out );
      out.close();
    } );
  }

  /**
   * Marks text as left-to-right (e.g. for English words embedded in
   * a right-to-left paragraph).
   *
   * @param text Text in a left-to-right script.
   * @return New RtfText object with LTR direction.
   */
  public static RtfText leftToRight( Object text ) {
    RtfText inner = text( text );
    return new RtfText( out -> {
      out.open( RtfControlWords.LEFT_TO_RIGHT_CHAR ).sp();
      inner.rtf( out );
      out.close();
    } );
  }
}
