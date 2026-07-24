package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RtfUnitTest {

  @Test void twipsArePassedThrough() {
    assertThat( RtfUnit.TWIPS.toTwips( 100 ) ).isEqualTo( 100 );
  }

  @Test void pointIsTwentyTwips() {
    assertThat( RtfUnit.POINT.toTwips( 1 ) ).isEqualTo( 20 );
    assertThat( RtfUnit.POINT.toTwips( 12 ) ).isEqualTo( 240 );
  }

  @Test void inchIs1440Twips() {
    assertThat( RtfUnit.INCH.toTwips( 1 ) ).isEqualTo( 1440 );
  }

  @Test void cmIsRounded() {
    assertThat( RtfUnit.CM.toTwips( 1 ) ).isEqualTo( 567 );
    assertThat( RtfUnit.CM.toTwips( 10 ) ).isEqualTo( 5669 );
  }

  @Test void mmIsRounded() {
    assertThat( RtfUnit.MM.toTwips( 1 ) ).isEqualTo( 57 );
    assertThat( RtfUnit.MM.toTwips( 10 ) ).isEqualTo( 567 );
  }

  @Test void fractionalValuesAreTruncatedForIntegerUnits() {
    assertThat( RtfUnit.TWIPS.toTwips( 99.9 ) ).isEqualTo( 99 );
    assertThat( RtfUnit.POINT.toTwips( 1.5 ) ).isEqualTo( 30 );
  }
}
