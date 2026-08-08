package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RtfHeaderStyleTest {

  private static String rtf( RtfHeaderStyle style ) {
    StringBuilder sb = new StringBuilder( 128 );
    style.rtf( new RtfOutput( sb ) );
    return sb.toString();
  }

  @Test void builtinsHaveFixedIdsAndAreStableAcrossCalls() {
    assertThat( RtfHeaderStyle.NORMAL.getId() ).isEqualTo( 0 );
    assertThat( RtfHeaderStyle.HEADER_1.getId() ).isEqualTo( 1 );
    assertThat( RtfHeaderStyle.HEADER_5.getId() ).isEqualTo( 5 );
    assertThat( RtfHeaderStyle.builtins()[ 0 ] ).isSameAs( RtfHeaderStyle.NORMAL );
    assertThat( RtfHeaderStyle.builtins() ).isNotSameAs( RtfHeaderStyle.builtins() );
  }

  @Test void builderStyleIsNotEvaluatedUntilWrite() {
    RtfHeaderStyle style = RtfHeaderStyle.builder( "S" ).build();
    Rtf.rtf().headerStyles( style );           // assigns id, does not render
    String firstWrite = rtf( style );           // renders
    assertThat( firstWrite ).isEqualTo( "{\\s0  S;}" );
    String secondWrite = rtf( style );          // renders again (no caching)
    assertThat( secondWrite ).isEqualTo( "{\\s0  S;}" );
  }

  @Test void builderRendersNameAndBasedOnAndFormatting() {
    RtfHeaderStyle style = RtfHeaderStyle.builder( "MyStyle" )
        .basedOn( RtfHeaderStyle.NORMAL ).build();
    Rtf.rtf().headerStyles( style );
    assertThat( rtf( style ) ).isEqualTo( "{\\s0 \\sbasedon0 MyStyle;}" );
  }

  @Test void builderRejectsMissingName() {
    assertThatIllegalArgumentException().isThrownBy( () -> RtfHeaderStyle.builder( "" ) );
    assertThatIllegalArgumentException().isThrownBy( () -> RtfHeaderStyle.builder( null ) );
  }

  @Test void builderComposesBasedOnFontAndAlignment() {
    RtfHeaderStyle style = RtfHeaderStyle.builder( "Built" )
        .basedOn( RtfHeaderStyle.NORMAL ).font( 0 ).fontSize( 24 ).bold().alignCentered().build();
    Rtf.rtf().headerStyles( style );
    assertThat( rtf( style ) ).isEqualTo( "{\\s0 \\sbasedon0\\f0\\fs24\\b\\qc Built;}" );
  }

  @Test void assignedIdIsNotOverwrittenOnSecondRegistration() {
    RtfHeaderStyle style = RtfHeaderStyle.builder( "S" ).build();
    Rtf.rtf().headerStyles( style );
    int firstId = style.getId();
    Rtf.rtf().headerStyles( style ); // register with a different, fresh document
    assertThat( style.getId() ).isEqualTo( firstId );
  }
}
