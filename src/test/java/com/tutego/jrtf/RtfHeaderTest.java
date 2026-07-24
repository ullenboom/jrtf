package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RtfHeaderTest {

  @Test void colorIndexOutOfRangeIsRejected() {
    assertThatExceptionOfType( RtfException.class ).isThrownBy( () -> RtfHeader.color( 1, 2, 3 ).at( 0 ) );
    assertThatExceptionOfType( RtfException.class ).isThrownBy( () -> RtfHeader.color( 1, 2, 3 ).at( 256 ) );
  }

  @Test void colorValuesAreMaskedToOneByte() {
    String out = Rtf.rtf().header( RtfHeader.color( 0x1FF, 0x2A, 0x00 ).at( 1 ) )
                    .p( RtfText.color( 1, "x" ) ).toString();
    // 0x1FF & 0xFF = 255
    assertThat( out ).contains( "\\red255\\green42\\blue0;" );
  }

  @Test void fontInfoWritesFamilyCharsetAndPitch() throws IOException {
    StringBuilder sb = new StringBuilder();
    RtfHeaderFont font = (RtfHeaderFont) RtfHeader.font( "Arial" )
        .family( RtfHeaderFont.FontFamily.SWISS )
        .charset( RtfHeaderFont.CharSet.ANSI )
        .pitch( RtfHeaderFont.Pitch.VARIABLE )
        .at( 2 );
    font.writeFontInfo( sb );
    assertThat( sb.toString() ).isEqualTo( "{\\f2\\fswiss\\fcharset0\\fprq2 Arial;}" );
  }
}
