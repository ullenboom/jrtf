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
import static com.tutego.jrtf.RtfFields.tableOfContentsField;
import static com.tutego.jrtf.RtfPara.*;
import java.awt.Desktop;
import java.io.*;
import com.tutego.jrtf.*;
//import com.tutego.jrtf.RtfPicture.PictureType;

/**
 * Example class.
 */
public class RtfTOCDemo
{
  /**
   * Start application.
   * @param args Program arguments.
   * @throws IOException If something goes wrong during writing.
   */
  public static void main( String... args ) throws IOException
  {
    File out = new File( "out-toc.rtf" );

    rtf()
    	.headerStyles(RtfHeaderStyle.values())
    	.section(p("Table of content\n"),p(tableOfContentsField()))
    	.section(
    			p(RtfHeaderStyle.HEADER_1, "Style - Header 1"),
    			p(RtfHeaderStyle.HEADER_2, "Style - Header 2"),
    			p(RtfHeaderStyle.HEADER_3, "Style - Header 3"),
    			p(RtfHeaderStyle.HEADER_1, "Style - Header 1"),
    			p(RtfHeaderStyle.HEADER_2, "Style - Header 2")
    	)
    	.out( new FileWriter( out ) );

    try
    {
      Desktop.getDesktop().open( out );
    }
    catch ( IOException e ) { e.printStackTrace(); }
  }
}
