package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RtfDocfmtTest {

  private static String rtf( RtfDocfmt docfmt ) {
    StringBuilder sb = new StringBuilder();
    docfmt.rtf( new RtfOutput( sb ) );
    return sb.toString();
  }

  @Test void paperWidthConvertsUnitToTwips() {
    assertThat( rtf( RtfDocfmt.paperWidth( 1, RtfUnit.INCH ) ) ).isEqualTo( "\\paperw1440" );
  }

  @Test void paperCombinesWidthAndHeight() {
    assertThat( rtf( RtfDocfmt.paper( 1, 2, RtfUnit.INCH ) ) ).isEqualTo( "\\paperw1440\\paperh2880" );
  }

  @Test void leftMargin() {
    assertThat( rtf( RtfDocfmt.leftMargin( 1, RtfUnit.INCH ) ) ).isEqualTo( "\\margl1440" );
  }

  @Test void landscape() {
    assertThat( rtf( RtfDocfmt.landscape() ) ).isEqualTo( "\\landscape" );
  }

  @Test void a4PaperFormatIsPortraitCmConvertedToTwips() {
    assertThat( rtf( RtfDocfmt.A4 ) ).isEqualTo( "\\paperw11905\\paperh16837" );
  }
}
