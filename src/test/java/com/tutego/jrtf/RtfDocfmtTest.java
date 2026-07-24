package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RtfDocfmtTest {

  @Test void paperWidthConvertsUnitToTwips() {
    assertThat( RtfDocfmt.paperWidth( 1, RtfUnit.INCH ).rtf ).isEqualTo( "\\paperw1440" );
  }

  @Test void paperCombinesWidthAndHeight() {
    assertThat( RtfDocfmt.paper( 1, 2, RtfUnit.INCH ).rtf ).isEqualTo( "\\paperw1440\\paperh2880" );
  }

  @Test void leftMargin() {
    assertThat( RtfDocfmt.leftMargin( 1, RtfUnit.INCH ).rtf ).isEqualTo( "\\margl1440" );
  }

  @Test void landscape() {
    assertThat( RtfDocfmt.landscape().rtf ).isEqualTo( "\\landscape" );
  }

  @Test void a4PaperFormatIsPortraitCmConvertedToTwips() {
    assertThat( RtfDocfmt.A4.rtf ).isEqualTo( "\\paperw16837\\paperh11905" );
  }
}
