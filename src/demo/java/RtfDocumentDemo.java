//import static com.tutego.jrtf.Rtf.document;
//import static com.tutego.jrtf.RtfDocfmt.*;
//import static com.tutego.jrtf.RtfHeader.*;
//import static com.tutego.jrtf.RtfInfo.*;
//import static com.tutego.jrtf.RtfFields.*;
//import static com.tutego.jrtf.RtfPara.*;
//import static com.tutego.jrtf.RtfSectionFormatAndHeaderFooter.*;
//import static com.tutego.jrtf.RtfText.*;
//import static com.tutego.jrtf.RtfUnit.*;
import static com.tutego.jrtf.Rtf.rtf;
import static com.tutego.jrtf.RtfHeader.font;
import static com.tutego.jrtf.RtfPara.*;
import static com.tutego.jrtf.RtfText.*;
import static com.tutego.jrtf.RtfUnit.CM;
import java.awt.Desktop;
import java.io.*;
import java.util.Date;
import com.tutego.jrtf.*;
//import com.tutego.jrtf.RtfPicture.PictureType;

/**
 * Example class.
 */
public class RtfDocumentDemo
{
  /**
   * Starts application.
   * @param args Program arguments.
   * @throws IOException If something goes wrong during writing.
   */
  public static void main( String... args ) throws IOException
  {
    File out = new File( "out.rtf" );

    RtfPara nextPar = RtfPara.p( "second paragraph" );

    rtf().
      header( font( RtfHeaderFont.WINDINGS ).at( 1 ) ).
      section(
        p( "line1 ", 2, bold(" 3"), new Date(), text("dd"), text(true, "1", 2) ),
        nextPar,
        row( bold( "Üöäß" ),
             bold( "T" )
        ).bottomCellBorder().topCellBorder().cellSpace( 1, CM )
//,
//        row( picture( RtfDocumentDemo.class.getResource( "folder.png" ) ).type( PictureType.AUTOMATIC ),
//             font(1, text("\u00fc") )
//        ).cellSpace( 1, CM )
      ).out( new FileWriter( out ) );

    try
    {
      Desktop.getDesktop().open( out );
    }
    catch ( IOException e ) { e.printStackTrace(); }
  }
}
