import static com.tutego.jrtf.Rtf.rtf;
import static com.tutego.jrtf.RtfFields.tableOfContentsField;
import static com.tutego.jrtf.RtfPara.*;
import com.tutego.jrtf.*;
import java.awt.Desktop;
import java.io.*;

/**
 * Example class.
 */
public class RtfTOCDemo
{
  /**
   * Starts demo application.
   * @param args Program arguments.
   * @throws IOException If something goes wrong during writing.
   */
  public static void main( String... args ) throws IOException
  {
    File out = new File( "out-toc.rtf" );

    rtf().headerStyles( RtfHeaderStyle.values() )
         .section( p( "Table of content\n" ),
                   p( tableOfContentsField() ) )
        .section( p( RtfHeaderStyle.HEADER_1, "Style - Header 1" ),
                  p( RtfHeaderStyle.HEADER_2, "Style - Header 2" ),
                  p( RtfHeaderStyle.HEADER_3, "Style - Header 3" ),
                  p( RtfHeaderStyle.HEADER_1, "Style - Header 1" ),
                  p( RtfHeaderStyle.HEADER_2, "Style - Header 2" ) )
    	.out( new FileWriter( out ) );
    try
    {
      Desktop.getDesktop().open( out );
    }
    catch ( IOException e ) { e.printStackTrace(); }
  }
}