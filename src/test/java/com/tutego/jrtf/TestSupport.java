package com.tutego.jrtf;

import java.io.IOException;

/**
 * Renders package-private RTF-producing objects to a String for test assertions,
 * without adding any public API surface to the main sources.
 */
final class TestSupport {
  private TestSupport() {}

  static String rtf( RtfText text ) {
    StringBuilder sb = new StringBuilder();
    try {
      text.rtf( sb );
    }
    catch ( IOException e ) {
      throw new RtfException( e );
    }
    return sb.toString();
  }

  static String rtf( RtfPara para, boolean withEndingPar ) {
    StringBuilder sb = new StringBuilder();
    try {
      para.rtf( sb, withEndingPar );
    }
    catch ( IOException e ) {
      throw new RtfException( e );
    }
    return sb.toString();
  }
}
