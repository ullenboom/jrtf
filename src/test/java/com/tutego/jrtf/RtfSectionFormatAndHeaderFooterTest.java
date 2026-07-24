package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RtfSectionFormatAndHeaderFooterTest {

  @Test void sectionFormattingConcatenatesInOrder() {
    RtfSectionFormatAndHeaderFooter combined = RtfSectionFormatAndHeaderFooter.sectionFormatting(
        RtfSectionFormatAndHeaderFooter.columns( 2 ),
        RtfSectionFormatAndHeaderFooter.titlePage() );
    assertThat( combined.rtf.toString() ).isEqualTo( "\\cols2\\titlepg" );
  }

  @Test void columnsRejectsNonPositive() {
    assertThatExceptionOfType( RtfException.class )
        .isThrownBy( () -> RtfSectionFormatAndHeaderFooter.columns( 0 ) );
  }

  @Test void pageNumberFormats() {
    assertThat( RtfSectionFormatAndHeaderFooter.pageNumberLowerRoman().rtf.toString() ).isEqualTo( "\\pgnlcrm" );
    assertThat( RtfSectionFormatAndHeaderFooter.pageNumberUpperRoman().rtf.toString() ).isEqualTo( "\\pgnucrm" );
    assertThat( RtfSectionFormatAndHeaderFooter.pageNumberDecimal().rtf.toString() ).isEqualTo( "\\pgndec" );
  }

  @Test void titlePage() {
    assertThat( RtfSectionFormatAndHeaderFooter.titlePage().rtf.toString() ).isEqualTo( "\\titlepg" );
  }

  @Test void headerForAllPagesFramesParagraph() {
    assertThat( RtfSectionFormatAndHeaderFooter.headerForAllPages( RtfPara.p( "H" ) ).rtf.toString() )
        .isEqualTo( "{\\header{\\s0 H\\par}\n}" );
  }

  @Test void sectionAppearsBeforeParagraphsInDocument() {
    Rtf rtf = Rtf.rtf();
    rtf.section( RtfSectionFormatAndHeaderFooter.columns( 2 ), RtfPara.p( "Body" ) );
    String out = rtf.toString();
    assertThat( out.indexOf( "\\cols2" ) ).isLessThan( out.indexOf( "Body" ) );
  }
}
