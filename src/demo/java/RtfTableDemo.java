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
import static com.tutego.jrtf.RtfPara.*;
import java.awt.Desktop;
import java.io.*;
import com.tutego.jrtf.*;
//import com.tutego.jrtf.RtfPicture.PictureType;

/**
 * Example class.
 */
public class RtfTableDemo
{
  /**
   * Start application.
   * @param args Program arguments.
   * @throws IOException If something goes wrong during writing.
   */
  public static void main( String... args ) throws IOException
  {
    File out = new File( "out-table.rtf" );
    
    RtfHeader red = RtfHeader.color(0xff, 0, 0).at(0);
    RtfHeader green = RtfHeader.color(0, 0xff, 0).at(1);
    RtfHeader blue = RtfHeader.color(0, 0, 0xff).at(2);

    rtf()
    	.header(red, green, blue)
    	.section(
    			row(p("ROW WITHOUT A BACKGROUND COLOR")),
    			row(0 ,p("RED").cellWidth(10, RtfUnit.CM)),
    			row(1 ,"GREEN"),
    			row(2 ,RtfText.text("BLUE")),
    			row(3 ,p("COLOR NOT FOUND"))
    	)
    	.out( new FileWriter( out ) );   			

    try
    {
      Desktop.getDesktop().open( out );
    }
    catch ( IOException e ) { e.printStackTrace(); }
  }
}
