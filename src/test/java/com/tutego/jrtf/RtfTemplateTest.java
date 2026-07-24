package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RtfTemplateTest {

  private static InputStream stream( String content ) {
    return new ByteArrayInputStream( content.getBytes( StandardCharsets.ISO_8859_1 ) );
  }

  @Test void substitutesSingleVariable() {
    String out = Rtf.template( stream( "Hello %%NAME%%!" ) )
                    .inject( "NAME", "tutego" )
                    .out();
    assertThat( out ).isEqualTo( "Hello tutego!" );
  }

  @Test void substitutesFromMap() {
    Map<String, Object> vars = new HashMap<>();
    vars.put( "A", "1" );
    vars.put( "B", "2" );
    String out = Rtf.template( stream( "%%A%% and %%B%%" ) ).inject( vars ).out();
    assertThat( out ).isEqualTo( "1 and 2" );
  }

  @Test void unknownVariableIsLeftUntouched() {
    String out = Rtf.template( stream( "Hi %%UNKNOWN%%" ) ).out();
    assertThat( out ).isEqualTo( "Hi %%UNKNOWN%%" );
  }

  @Test void nullValueIsSubstitutedAsLiteralNull() {
    String out = Rtf.template( stream( "%%X%%" ) ).inject( "X", null ).out();
    assertThat( out ).isEqualTo( "null" );
  }

  @Test void rtfTextValueIsRenderedAsRtf() {
    String out = Rtf.template( stream( "%%X%%" ) ).inject( "X", RtfText.bold( "b" ) ).out();
    assertThat( out ).isEqualTo( "{\\b b}" );
  }

  @Test void templateWithoutVariablesIsPassedThroughUnchanged() {
    String out = Rtf.template( stream( "plain text, no vars" ) ).out();
    assertThat( out ).isEqualTo( "plain text, no vars" );
  }

  @Test void streamIsNotReadBeforeOutIsCalled() {
    boolean[] wasRead = { false };
    InputStream tracking = new InputStream() {
      @Override public int read() {
        wasRead[ 0 ] = true;
        return -1;
      }
    };

    RtfTemplate template = Rtf.template( tracking );
    assertThat( wasRead[ 0 ] ).as( "constructor must not read the stream" ).isFalse();

    template.out();
    assertThat( wasRead[ 0 ] ).as( "out() must read the stream" ).isTrue();
  }

  @Test void nullInputStreamIsRejected() {
    org.assertj.core.api.Assertions.assertThatExceptionOfType( IllegalArgumentException.class )
        .isThrownBy( () -> Rtf.template( null ) );
  }

  @Test void nullKeyIsRejected() {
    org.assertj.core.api.Assertions.assertThatExceptionOfType( IllegalArgumentException.class )
        .isThrownBy( () -> Rtf.template( stream( "x" ) ).inject( null, "v" ) );
  }
}
