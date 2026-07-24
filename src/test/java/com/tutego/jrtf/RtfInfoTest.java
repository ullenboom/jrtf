package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RtfInfoTest {

  @Test void subjectProducesSubjectGroup() {
    assertThat( RtfInfo.subject( "Invoice" ).rtf ).isEqualTo( "{\\subject Invoice}" );
  }

  @Test void titleProducesTitleGroup() {
    assertThat( RtfInfo.title( "My Document" ).rtf ).isEqualTo( "{\\title My Document}" );
  }

  @Test void authorProducesAuthorGroup() {
    assertThat( RtfInfo.author( "tutego" ).rtf ).isEqualTo( "{\\author tutego}" );
  }

  @Test void keywordsProducesKeywordsGroup() {
    assertThat( RtfInfo.keywords( "rtf java" ).rtf ).isEqualTo( "{\\keywords rtf java}" );
  }

  @Test void commentProducesCommentGroup() {
    assertThat( RtfInfo.comment( "internal note" ).rtf ).isEqualTo( "{\\comment internal note}" );
  }

  @Test void operatorProducesOperatorGroup() {
    assertThat( RtfInfo.operator( "cu" ).rtf ).isEqualTo( "{\\operator cu}" );
  }

  @Test void doccommProducesDoccommGroup() {
    assertThat( RtfInfo.doccomm( "draft" ).rtf ).isEqualTo( "{\\doccomm draft}" );
  }

  @Test void versionProducesVersionGroup() {
    assertThat( RtfInfo.version( 3 ).rtf ).isEqualTo( "{\\version3}" );
  }

  @Test void numberOfWordsProducesNofwordsGroup() {
    assertThat( RtfInfo.numberOfWords( 120 ).rtf ).isEqualTo( "{\\nofwords120}" );
  }

  @Test void numberOfPagesProducesNofpagesGroup() {
    assertThat( RtfInfo.numberOfPages( 4 ).rtf ).isEqualTo( "{\\nofpages4}" );
  }

  @Test void creatimProducesTimeGroup() {
    assertThat( RtfInfo.creatim( 2026, 7, 23, 10, 30, 0 ).rtf )
        .isEqualTo( "{\\creatim \\yr2026 \\mo7 \\dy23 \\hr10 \\min30 \\sec0}" );
  }

  @Test void revtimProducesTimeGroup() {
    assertThat( RtfInfo.revtim( 2026, 7, 23, 11, 0, 0 ).rtf )
        .isEqualTo( "{\\revtim \\yr2026 \\mo7 \\dy23 \\hr11 \\min0 \\sec0}" );
  }

  @Test void printimProducesTimeGroup() {
    assertThat( RtfInfo.printim( 2026, 7, 23, 12, 0, 0 ).rtf )
        .isEqualTo( "{\\printim \\yr2026 \\mo7 \\dy23 \\hr12 \\min0 \\sec0}" );
  }

  @Test void infoIsWrittenIntoDocumentInfoGroup() {
    String out = Rtf.rtf().info( RtfInfo.title( "T" ), RtfInfo.author( "A" ) )
                          .p( "x" ).out().toString();
    assertThat( out ).contains( "{\\info{\\title T}{\\author A}}\n" );
  }
}
