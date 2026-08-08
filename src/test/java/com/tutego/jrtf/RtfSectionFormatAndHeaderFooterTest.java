package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RtfSectionFormatAndHeaderFooterTest {

  private static String rtf( RtfSectionFormatAndHeaderFooter fmt ) {
    StringBuilder sb = new StringBuilder();
    fmt.rtf( new RtfOutput( sb ) );
    return sb.toString();
  }

  @Test void sectionFormattingConcatenatesInOrder() {
    RtfSectionFormatAndHeaderFooter combined = RtfSectionFormatAndHeaderFooter.sectionFormatting(
        RtfSectionFormatAndHeaderFooter.columns( 2 ),
        RtfSectionFormatAndHeaderFooter.titlePage() );
    assertThat( rtf( combined ) ).isEqualTo( "\\cols2\\titlepg" );
  }

  @Test void columnsRejectsNonPositive() {
    assertThatExceptionOfType( RtfException.class )
        .isThrownBy( () -> RtfSectionFormatAndHeaderFooter.columns( 0 ) );
  }

  @Test void pageNumberFormats() {
    assertThat( rtf( RtfSectionFormatAndHeaderFooter.pageNumberLowerRoman() ) ).isEqualTo( "\\pgnlcrm" );
    assertThat( rtf( RtfSectionFormatAndHeaderFooter.pageNumberUpperRoman() ) ).isEqualTo( "\\pgnucrm" );
    assertThat( rtf( RtfSectionFormatAndHeaderFooter.pageNumberDecimal() ) ).isEqualTo( "\\pgndec" );
  }

  @Test void titlePage() {
    assertThat( rtf( RtfSectionFormatAndHeaderFooter.titlePage() ) ).isEqualTo( "\\titlepg" );
  }

  @Test void headerForAllPagesFramesParagraph() {
    assertThat( rtf( RtfSectionFormatAndHeaderFooter.headerForAllPages( RtfPara.p( "H" ) ) ) )
        .isEqualTo( "{\\header{\\s0 H\\par}\n}" );
  }

  @Test void sectionAppearsBeforeParagraphsInDocument() {
    Rtf rtf = Rtf.rtf();
    rtf.section( RtfSectionFormatAndHeaderFooter.columns( 2 ), RtfPara.p( "Body" ) );
    String out = rtf.toString();
    assertThat( out.indexOf( "\\cols2" ) ).isLessThan( out.indexOf( "Body" ) );
  }
}
