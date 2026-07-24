package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.tutego.jrtf.TestSupport.rtf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RtfPictureTest {

  @Test void streamIsNotReadBeforeTheDocumentIsWritten() {
    boolean[] wasRead = { false };
    InputStream tracking = new InputStream() {
      @Override public int read() {
        wasRead[ 0 ] = true;
        return -1;
      }
    };

    RtfText pic = RtfText.picture( tracking ).type( RtfPicture.PictureType.PNG );
    assertThat( wasRead[ 0 ] ).as( "picture() must not read the stream" ).isFalse();

    rtf( pic );
    assertThat( wasRead[ 0 ] ).as( "writing the document must read the stream" ).isTrue();
  }

  @Test void explicitEmfTypeEmitsBlipAndHexData() {
    byte[] data = { 0x01, (byte) 0xAB, (byte) 0xFF };
    RtfText pic = RtfText.picture( new ByteArrayInputStream( data ) )
                         .type( RtfPicture.PictureType.EMF );
    assertThat( rtf( pic ) ).isEqualTo( "{\\pict\\emfblip\n01abff}" );
  }

  @Test void widthHeightAndScaleAreEmitted() {
    byte[] data = { 0x01, 0x02, 0x03 };
    RtfText pic = RtfText.picture( new ByteArrayInputStream( data ) )
                         .size( 2, 1, RtfUnit.CM ).scale( 50, 75 )
                         .type( RtfPicture.PictureType.PNG );
    String out = rtf( pic );
    assertThat( out ).contains( "\\pngblip", "\\picwgoal1134", "\\pichgoal567", "\\picscalex50", "\\picscaley75" );
  }

  @Test void automaticTypeRejectsUnknownImage() {
    byte[] data = new byte[ 20 ]; // all zeros, matches neither JPG nor PNG
    RtfText pic = RtfText.picture( new ByteArrayInputStream( data ) )
                         .type( RtfPicture.PictureType.AUTOMATIC );
    assertThatExceptionOfType( RtfException.class ).isThrownBy( () -> rtf( pic ) );
  }
}
