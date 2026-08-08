package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RtfUnicodeTest {

  @Test void asciiBelow127PassesThroughUnchanged() {
    assertThat( Rtf.asRtf( "Hello World 123" ) ).isEqualTo( "Hello World 123" );
  }

  @Test void newlineBecomesPar() {
    assertThat( Rtf.asRtf( "\n" ) ).isEqualTo( "\\par\n" );
  }

  @Test void tabBecomesTabControlWord() {
    assertThat( Rtf.asRtf( "\t" ) ).isEqualTo( "\\tab\n" );
  }

  @Test void backslashIsEscaped() {
    assertThat( Rtf.asRtf( "\\" ) ).isEqualTo( "\\\\" );
  }

  @Test void bracesAreEscaped() {
    assertThat( Rtf.asRtf( "{}" ) ).isEqualTo( "\\{\\}" );
  }

  @Test void windows1252MappableCharGetsUnicodeEscapeAndByteFallback() {
    // 'e' with acute accent, U+00E9, is byte 0xE9 in windows-1252
    assertThat( Rtf.asRtf( "é" ) ).isEqualTo( "\\u233\\'e9" );
  }

  @Test void charOutsideWindows1252UsesQuestionMarkFallback() {
    // U+0100 (Latin Capital Letter A with Macron) is not representable in windows-1252
    assertThat( Rtf.asRtf( "Ā" ) ).isEqualTo( "\\u256?" );
  }

  @Test void highSurrogateBmpCodepointUsesSignedShortValue() {
    // U+8000 (32768) is above the signed short range and must be written as a negative value
    assertThat( Rtf.asRtf( "耀" ) ).isEqualTo( "\\u-32768?" );
  }

  @Test void codepointAboveBmpIsWrittenAsTwoSignedSurrogateEscapes() {
    // U+1F600 GRINNING FACE = surrogate pair 😀
    String emoji = new String( Character.toChars( 0x1F600 ) );
    assertThat( Rtf.asRtf( emoji ) ).isEqualTo( "\\u-10179?\\u-8704?" );
  }
}
