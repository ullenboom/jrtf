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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A modern RTF list definition ({@code \listtable} / {@code \listoverridetable}), used to
 * produce genuine, interactively renumbered bulleted or numbered lists (as opposed to the
 * legacy, literal-text bullets/numbers of {@link RtfPara#ul(RtfText)} / {@link RtfPara#ol}).
 * <p>
 * A list has up to 9 independent levels (0-based), each with its own {@link NumberFormat},
 * start value and indentation. Levels are configured lazily: only the levels actually used
 * need to be touched; higher, unconfigured levels fall back to a bullet with increasing indent.
 * Each level numbers independently &mdash; this implementation does not support the fully
 * hierarchical, ancestor-concatenating numbering (e.g. {@code "1.1.1."}) the RTF format also
 * allows.
 * <p>
 * A list must be registered on the document with {@link Rtf#lists(RtfList...)} before any
 * paragraph can reference it via {@link RtfTextPara#list(RtfList, int)}. Like everything else
 * in this library, no RTF is produced until the document is written.
 * <pre>
 * import static com.tutego.jrtf.RtfList.NumberFormat.DECIMAL;
 *
 * RtfList bullets = RtfList.bulleted();
 * RtfList numbers = RtfList.numbered( DECIMAL );
 *
 * Rtf.rtf().lists( bullets, numbers )
 *          .section( RtfPara.p( "a" ).list( bullets, 0 ),
 *                    RtfPara.p( "b" ).list( bullets, 0 ),
 *                    RtfPara.p( "1" ).list( numbers, 0 ) )
 *          .out( new FileWriter( "out.rtf" ) );
 * </pre>
 */
public final class RtfList {
  /**
   * Number format of a list level.
   */
  public enum NumberFormat {
    /**
     * Bulleted, e.g. &bull;
     */
    BULLET( 23 ),

    /**
     * Arabic numerals: 1, 2, 3, &hellip;
     */
    DECIMAL( 0 ),

    /**
     * Uppercase Roman numerals: I, II, III, &hellip;
     */
    UPPER_ROMAN( 1 ),

    /**
     * Lowercase Roman numerals: i, ii, iii, &hellip;
     */
    LOWER_ROMAN( 2 ),

    /**
     * Uppercase letters: A, B, C, &hellip;
     */
    UPPER_LETTER( 3 ),

    /**
     * Lowercase letters: a, b, c, &hellip;
     */
    LOWER_LETTER( 4 );

    final int levelnfc;

    NumberFormat( int levelnfc ) {
      this.levelnfc = levelnfc;
    }
  }

  /**
   * Bullet character (Windows-1252 code point 0x95, &bull;) used for {@link NumberFormat#BULLET}.
   */
  private static final int BULLET_CHAR = 0x95;

  private static final AtomicInteger NEXT_LIST_ID = new AtomicInteger( 1 );

  /**
   * Unique, process-wide id of this list ({@code \listid}).
   */
  final int listId = NEXT_LIST_ID.getAndIncrement();

  /**
   * 1-based index into the document's list override table ({@code \ls}), assigned by
   * {@link Rtf#lists(RtfList...)}. {@code -1} until registered.
   */
  int overrideIndex = -1;

  private final List<Level> levels = new ArrayList<>();

  private static final class Level {
    NumberFormat format;
    String suffix;
    int startAt = 1;
    int indentTwips;
    final int hangingTwips = 360;
  }

  private RtfList() {}

  private static Level newLevel( int levelIndex, NumberFormat format ) {
    Level level = new Level();
    level.format = format;
    level.suffix = format == NumberFormat.BULLET ? "" : ".";
    level.indentTwips = 720 * (levelIndex + 1);
    return level;
  }

  /**
   * Creates a list whose first level (0) is bulleted.
   *
   * @return New list. Not yet usable until registered with {@link Rtf#lists(RtfList...)}.
   */
  public static RtfList bulleted() {
    RtfList list = new RtfList();
    list.levels.add( newLevel( 0, NumberFormat.BULLET ) );
    return list;
  }

  /**
   * Creates a list whose first level (0) is numbered with the given format.
   *
   * @param format Number format of level 0.
   * @return New list. Not yet usable until registered with {@link Rtf#lists(RtfList...)}.
   */
  public static RtfList numbered( NumberFormat format ) {
    if ( format == null )
      throw new IllegalArgumentException( "Number format can't be null" );

    RtfList list = new RtfList();
    list.levels.add( newLevel( 0, format ) );
    return list;
  }

  /**
   * Configures the number format of an (0-based) level of this list. Levels between an
   * already configured level and {@code levelIndex} that were not explicitly configured
   * default to a bullet with increasing indentation.
   *
   * @param levelIndex 0-based level, 0 to 8.
   * @param format     Number format for this level.
   * @return {@code this}-object.
   */
  public RtfList level( int levelIndex, NumberFormat format ) {
    if ( format == null )
      throw new IllegalArgumentException( "Number format can't be null" );

    ensureLevel( levelIndex ).format = format;
    return this;
  }

  /**
   * Sets the start value of level 0.
   *
   * @param startAt Start value (usually 1).
   * @return {@code this}-object.
   */
  public RtfList startAt( int startAt ) {
    return startAt( 0, startAt );
  }

  /**
   * Sets the start value of the given level.
   *
   * @param levelIndex 0-based level, 0 to 8.
   * @param startAt    Start value (usually 1).
   * @return {@code this}-object.
   */
  public RtfList startAt( int levelIndex, int startAt ) {
    ensureLevel( levelIndex ).startAt = startAt;
    return this;
  }

  /**
   * Sets the text following the number/bullet of level 0. Default is {@code "."} for
   * numbered levels and {@code ""} for bulleted levels.
   *
   * @param suffix Text after the number/bullet. Must not be {@code null}.
   * @return {@code this}-object.
   */
  public RtfList suffix( String suffix ) {
    return suffix( 0, suffix );
  }

  /**
   * Sets the text following the number/bullet of the given level.
   *
   * @param levelIndex 0-based level, 0 to 8.
   * @param suffix     Text after the number/bullet. Must not be {@code null}.
   * @return {@code this}-object.
   */
  public RtfList suffix( int levelIndex, String suffix ) {
    if ( suffix == null )
      throw new IllegalArgumentException( "Suffix can't be null" );

    ensureLevel( levelIndex ).suffix = suffix;
    return this;
  }

  /**
   * Sets the left indent of level 0. Default is 720 twips (0.5 inch).
   *
   * @param indent Indent.
   * @param unit   Measurement unit.
   * @return {@code this}-object.
   */
  public RtfList indent( double indent, RtfUnit unit ) {
    return indent( 0, indent, unit );
  }

  /**
   * Sets the left indent of the given level. Default is {@code 720 * (levelIndex + 1)} twips.
   *
   * @param levelIndex 0-based level, 0 to 8.
   * @param indent     Indent.
   * @param unit       Measurement unit.
   * @return {@code this}-object.
   */
  public RtfList indent( int levelIndex, double indent, RtfUnit unit ) {
    ensureLevel( levelIndex ).indentTwips = unit.toTwips( Math.abs( indent ) );
    return this;
  }

  private Level ensureLevel( int levelIndex ) {
    if ( levelIndex < 0 || levelIndex > 8 )
      throw new IllegalArgumentException( "Level must be between 0 and 8 but is " + levelIndex );

    while ( levels.size() <= levelIndex )
      levels.add( newLevel( levels.size(), NumberFormat.BULLET ) );

    return levels.get( levelIndex );
  }

  /**
   * Number of levels configured so far (1 to 9).
   *
   * @return Level count.
   */
  int levelCount() {
    return levels.size();
  }

  /**
   * Left indent in twips of the given level, for use by {@link RtfTextPara#list(RtfList, int)}.
   */
  int indentTwipsAt( int levelIndex ) {
    return levels.get( levelIndex ).indentTwips;
  }

  /**
   * Hanging (negative first-line) indent in twips of the given level.
   */
  int hangingTwipsAt( int levelIndex ) {
    return levels.get( levelIndex ).hangingTwips;
  }

  // Rendering

  private static String hexByte( int value ) {
    return String.format( RtfControlWords.HEX_ESCAPE_FORMAT, value & 0xFF );
  }

  /**
   * Writes the {@code \listlevel} group for one level, self-referencing its own (0-based)
   * index as the sole number placeholder (this implementation does not concatenate ancestor
   * level numbers).
   */
  private static void writeLevel( Appendable out, int levelIndex, Level level ) throws IOException {
    out.append( "{" ).append( RtfControlWords.LIST_LEVEL )
       .append( RtfControlWords.LEVEL_NUMBER_FORMAT ).append( Integer.toString( level.format.levelnfc ) )
       .append( RtfControlWords.LEVEL_JUSTIFICATION ).append( "0" )
       .append( RtfControlWords.LEVEL_FOLLOW ).append( "0" )
       .append( RtfControlWords.LEVEL_START_AT ).append( Integer.toString( level.startAt ) );

    out.append( "{" ).append( RtfControlWords.LEVEL_TEXT ).append( ' ' );
    if ( level.format == NumberFormat.BULLET ) {
      out.append( hexByte( 1 ) ).append( hexByte( BULLET_CHAR ) );
    }
    else {
      out.append( hexByte( 1 + level.suffix.length() ) ).append( hexByte( levelIndex ) );
      for ( int i = 0; i < level.suffix.length(); i++ )
        out.append( hexByte( level.suffix.charAt( i ) ) );
    }
    out.append( ";}" );

    out.append( "{" ).append( RtfControlWords.LEVEL_NUMBERS ).append( ' ' );
    if ( level.format != NumberFormat.BULLET )
      out.append( hexByte( 1 ) );
    out.append( ";}" );

    out.append( RtfControlWords.LEFT_INDENT ).append( Integer.toString( level.indentTwips ) )
       .append( RtfControlWords.FIRST_LINE_INDENT ).append( "-" ).append( Integer.toString( level.hangingTwips ) )
       .append( "}\n" );
  }

  /**
   * Writes this list's {@code \list} definition into the document's {@code \listtable}.
   * Per spec, a list with more than one level must define all 9 levels, so levels beyond
   * the ones configured are padded with a clone of the last configured level.
   */
  void writeListDefinition( Appendable out ) throws IOException {
    boolean simple = levels.size() <= 1;

    out.append( "{" ).append( RtfControlWords.LIST )
       .append( RtfControlWords.LIST_TEMPLATE_ID ).append( Integer.toString( listId * 1000 ) )
       .append( RtfControlWords.LIST_SIMPLE ).append( simple ? "1" : "0" ).append( '\n' );

    int levelsToWrite = simple ? 1 : 9;
    for ( int i = 0; i < levelsToWrite; i++ ) {
      Level level = i < levels.size() ? levels.get( i ) : levels.get( levels.size() - 1 );
      writeLevel( out, i, level );
    }

    out.append( RtfControlWords.LIST_ID ).append( Integer.toString( listId ) ).append( "}\n" );
  }

  /**
   * Writes this list's {@code \listoverride} entry into the document's {@code \listoverridetable}.
   */
  void writeListOverride( Appendable out ) throws IOException {
    out.append( "{" ).append( RtfControlWords.LIST_OVERRIDE )
       .append( RtfControlWords.LIST_ID ).append( Integer.toString( listId ) )
       .append( RtfControlWords.LIST_OVERRIDE_COUNT ).append( "0" )
       .append( RtfControlWords.LIST_OVERRIDE_INDEX ).append( Integer.toString( overrideIndex ) )
       .append( "}\n" );
  }
}
