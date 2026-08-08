package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RtfInfoTest {

  private static String rtf( RtfInfo info ) {
    StringBuilder sb = new StringBuilder();
    info.rtf( new RtfOutput( sb ) );
    return sb.toString();
  }

  @Test void subjectProducesSubjectGroup() {
    assertThat( rtf( RtfInfo.subject( "Invoice" ) ) ).isEqualTo( "{\\subject Invoice}" );
  }

  @Test void titleProducesTitleGroup() {
    assertThat( rtf( RtfInfo.title( "My Document" ) ) ).isEqualTo( "{\\title My Document}" );
  }

  @Test void authorProducesAuthorGroup() {
    assertThat( rtf( RtfInfo.author( "tutego" ) ) ).isEqualTo( "{\\author tutego}" );
  }

  @Test void keywordsProducesKeywordsGroup() {
    assertThat( rtf( RtfInfo.keywords( "rtf java" ) ) ).isEqualTo( "{\\keywords rtf java}" );
  }

  @Test void commentProducesCommentGroup() {
    assertThat( rtf( RtfInfo.comment( "internal note" ) ) ).isEqualTo( "{\\comment internal note}" );
  }

  @Test void operatorProducesOperatorGroup() {
    assertThat( rtf( RtfInfo.operator( "cu" ) ) ).isEqualTo( "{\\operator cu}" );
  }

  @Test void doccommProducesDoccommGroup() {
    assertThat( rtf( RtfInfo.doccomm( "draft" ) ) ).isEqualTo( "{\\doccomm draft}" );
  }

  @Test void versionProducesVersionGroup() {
    assertThat( rtf( RtfInfo.version( 3 ) ) ).isEqualTo( "{\\version3}" );
  }

  @Test void numberOfWordsProducesNofwordsGroup() {
    assertThat( rtf( RtfInfo.numberOfWords( 120 ) ) ).isEqualTo( "{\\nofwords120}" );
  }

  @Test void numberOfPagesProducesNofpagesGroup() {
    assertThat( rtf( RtfInfo.numberOfPages( 4 ) ) ).isEqualTo( "{\\nofpages4}" );
  }

  @Test void creatimProducesTimeGroup() {
    assertThat( rtf( RtfInfo.creatim( 2026, 7, 23, 10, 30, 0 ) ) )
        .isEqualTo( "{\\creatim \\yr2026 \\mo7 \\dy23 \\hr10 \\min30 \\sec0}" );
  }

  @Test void revtimProducesTimeGroup() {
    assertThat( rtf( RtfInfo.revtim( 2026, 7, 23, 11, 0, 0 ) ) )
        .isEqualTo( "{\\revtim \\yr2026 \\mo7 \\dy23 \\hr11 \\min0 \\sec0}" );
  }

  @Test void printimProducesTimeGroup() {
    assertThat( rtf( RtfInfo.printim( 2026, 7, 23, 12, 0, 0 ) ) )
        .isEqualTo( "{\\printim \\yr2026 \\mo7 \\dy23 \\hr12 \\min0 \\sec0}" );
  }

  @Test void infoIsWrittenIntoDocumentInfoGroup() {
    String out = Rtf.rtf().info( RtfInfo.title( "T" ), RtfInfo.author( "A" ) )
                          .p( "x" ).out().toString();
    assertThat( out ).contains( "{\\info{\\title T}{\\author A}}\n" );
  }
}
