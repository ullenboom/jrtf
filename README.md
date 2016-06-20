jRTF is a simple library to generate RTF documents and to fill RTF template files. The syntax is compact and non-verbose which makes it to some kind of DSL (domain specific language) for RTF documents. It's published under the BSD license.

See also:
* [jrtf in Maven Central](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jrtf%22)
* [Javadoc](http://javadoc.io/doc/com.tutego/jrtf/)

The Basics
==========

Write a simple RTF document to a file:

    Rtf.rtf().p( "Hello World" ).out( new FileWriter("out.rtf") );

Special RTF-characters like "{", "}", "\" are encoded automatically.
"\t" will stay tab and "\n" will be converted to a new paragraph.

The static `rtf()` creates a new RTF document object and the `p()` method is short for "paragraph". The `out()` method finally writes the output to an `Appendable` (a `Writer` for example) and `out()` without arguments or `toString()` returns the RTF document as String. The `p()` method is quite flexible because you can add as many parameters as you like. If during building or writing of the file some exceptions will occur they are all of type `RtfException` which itself is a `RuntimeException`. So I/O errors during writing will be wrapped in this `RtfException`. While calls to jRTF-objects are not synchronized the library itself does't save any state in static fields; this allows you to build several RTF documents at the same time and modify them at the same time.

Because jRTF makes heavy use of static methods the programs can be very concise and compact with static imports. Let's assume the following (static) imports for the next examples:

    import static com.tutego.jrtf.Rtf.rtf;
    import static com.tutego.jrtf.RtfDocfmt.*;
    import static com.tutego.jrtf.RtfHeader.*;
    import static com.tutego.jrtf.RtfInfo.*;
    import static com.tutego.jrtf.RtfFields.*;
    import static com.tutego.jrtf.RtfPara.*;
    import static com.tutego.jrtf.RtfSectionFormatAndHeaderFooter.*;
    import static com.tutego.jrtf.RtfText.*;
    import static com.tutego.jrtf.RtfUnit.*;


Sections
========

A RTF document consists of paragraphs which itself are arranged in sections.
A section is some kind of mini document with own header, footer, margins and columns.
Most documents consist of only one section.

The `p()` method of the `Rtf` class is just a façade for the following

    rtf().section( p( "Hello World" ) ).out( new FileWriter("out.rtf") );

If there will be more sections they are accumulated this way:

    rtf().section(xx).section(xx).section(xx).out(xx);

Paragraphs and Formattings
==========================

The following RTF document consists of several paragraphs and text formattings:

    rtf().section(
       p( "first paragraph" ),
       p( tab(),
          " second par ",
          bold( "with something in bold" ),
          text( " and " ),
          italic( underline( "italic underline" ) )     
        )  
    ).out( out );

The declaration of the section method is:

  * `Rtf section(RtfPara... paragraphs)`

For building paragraphs the `RtfPara` class offers two useful static methods: `p(RtfText...)` and a convenience method `p(Object...)`. So you either build a paragraph with a collection of `RtfText` objects or you pass a sequence of objects which will be converted to Strings (if the type is not already `RtfText`). `null` elements in the sequence will be ignored.

A String can be wrapped in a `RtfText` object via the static method `text(String)` of the `RtfText` class. There is also the vararg method `text(Object...)` with is the foundation for `p(Object...)`. An alternative method is `static RtfText textJoinWithSpaces( boolean joinWithSpace, final Object... texts )` that inserts a space if the first argument is `true`.

  * `static RtfText text( Object... texts )`
  * `static RtfText textJoinWithSpace( boolean joinWithSpace, final Object... texts )`

Beside of using ordinary text there are a couple of special methods like `tab()`, `bullet()`, `currentDateAbbreviated()`, `shortHyphen()` and more.


Special Text and Pictures
=========================

A footnote (`rtf()` omitted for brevity) is added as following:

    p( text("Read this book"),
       footnote( "The joy of RTF" ),
       text(".") )

Using a field requires two text blocks: One for the format description and one for the displayed value a reader can show (most recent calculated result of the field).

    field( p("time \\@ \"dd.MM.yyyy\""),
           p("20.06.2010") )

Because there are so many different fields and time is a common case there is an extra class `RtfFields` with static utility methods. Insert a time field with a pattern like this: `timeField("hh:mm:ss")`. Or the number of pages for a header:

    p( pageNumberField(), "von", sectionPagesField() )

Pictures are part of a paragraph. The source is given by an `URL` or `InputStream`. If the resource is not available a `RtfException` will be thrown during writing.

    p( picture( getClass().getResource( "folder.png" ) )
       .size( 64, 64, PIXEL )
       .type( AUTOMATIC )
     )

You can explicitly set the picture type to PNG or JPEG, but usually `PictureType.AUTOMATIC` will do the job.



Paragraph Formatting
====================


It you want a paragraph with bullets at the beginning use `ul()` instead of `p()`:

    section(
      p( "first paragraph" ),
      ul( "bullet1"),
      ul( "bullet2"),
      p( "another paragraph" )
    )

The `p()` methods return an `RtfTextPara` object which allows formatting the paragraph according to the builder pattern. Aligning a paragraph is done this way;

    p( text("centered and indented") ).alignCentered().indentFirstLine( 0.25, RtfUnit.CM )

`RtfUnit` is an enum. RTF uses a quite unique measurement (twips) but with the enum there is no need to know anything about twips.

If you want to use tabs do any of this:

    p( "1\t2\t3" ).tab( 3, CM )
                  .tab( TabKind.CENTER, TabLead.DOTS, 9, CM )

Additional to `p()` there is a method `pard()` where the paragraph styles are not inherited to the following paragraph.


Tables
======

Tables are a bit tricky in RTF because there isn't a concept of a table but just the concept of a row. In total a section can contain two different types of blocks: paragraphs and rows. While `p()` lets you insert a regular paragraph the method `row()` lets you insert a row.

    p( "lala" ),
    row( "Number", "Square" ),
    row( 1, 1 ),
    row( 2, 4 ),
    p( "lulu" )

While the result type of `p()` is `RtfTextPara` the result type of `row()` is `RtfRow`. With `RtfRow` you style the whole row in a similar way you style the paragraph.

    row(
      bold( "S" ), bold( "T" )
    ).bottomCellBorder().topCellBorder().cellSpace( 1, CM ),
    row(
      "Good", "nice"
    ). cellSpace( 1, CM )


A Bit of Style
==============

In order to use different fonts and colors a header has to precede the section:

    rtf()
      .header(
        color( 0xff, 0, 0 ).at( 0 ),
        color( 0, 0xff, 0 ).at( 1 ),
        color( 0, 0, 0xff ).at( 2 ),
        font( "Calibri" ).at( 0 ) )
      .section(
    	p( font( 1, "Second paragraph" ) ),
    	p( color( 1, "green" ) )
      )
    ).out( out );

This header is setting 3 colors and one font. Every color and font is identified by an index. This index is used later to identify this color and font. The numbering starts with 0. If there is no font given, "Times" will be the default font at position 0.

Some formats and styles are bound to a section, like a header. Let's set a header for all pages in that section:

    section(
      headerOnAllPages(
        p( "Date: ", currentDate() )
      ),
      p( "bla bla bla " )
    )


Metadata (Info, Document Formattings)
=====================================

A RTF document can have some associated meta data in a header, info or document info block. You can set this on the `rtf()` object:

    rtf()
      .info( author("christian"), title("without parental guidance") )
      .documentFormatting( defaultTab( 1, CM ),
                           revisionMarking() )
      .section("")
      .out( out );


Templating with jRTF
====================

jRTF is not able to read and change existing RTF documents (although I encourage programmers to extend jRTF) but you can inject `RtfText` in slots. If you want to do so prepare a RTF document with any Word processor and define "variables" which are framed in %%. If for example an address has to be written in the RTF document put a definition like

    %%ADDRESSLINE1%%

in the regular text.
(Take care not to change the formattings in between. If jRTF is not substituting your variable open the RTF file and check if the variable is really in the format `%%VARIABLE%%`. Use only pure ASCII variables.)

To substitute use the following jRTF API:

    Rtf.template( new FileInputStream("template.rtf") )
       .inject( "ADDRESSLINE1", "tutego" )
       .inject( "ADDRESSLINE2", bold("Sonsbeck") )
       .out( FileOutputStream("out.rtf") );

The key is always of type `String` but the value argument for `inject()` goes to `RtfText.text(Object)`. That means regular Strings or formatted RTF text is both fine.


jRTF Design Decisions
=====================


Several facts drove the design of jRTF.

  * The main design of the API was driven by ease of use. Take a paragraph and text for example. They are represented by the classes `RtfPara` and `RtfText`. But most methods are overloaded and as an alternative to `RtfPara/RtfText` they simply accept a regular `String` for the common usage or even an sequence of objects with are gracefully converted. 
  * jRTF is able to run on the Google App Engine because classes like `Color`, `Font`, `FileWriter` and other black-listed classes aren't used.
  * It should not be possible to generate illegal RTF and the library should throw exceptions if strange things happen. (It could do better...)
  * You will find three kinds of API styles
     * Hierarchical style.  The document model of jRTF is strictly hierarchical. A header can be added to the document but not to a text. A paragraph can just be part of a document but not part of a header, etc.
     * Varargs container style. If more than one element has to be added to some kind of container varargs are the preferred way and not collection classes, which are usually used in Java. For example: A section can contain several paragraphs, a paragraph several text parts, a document different headers, etc.
     * Fluent interface style. The fluent interface design has been usually chosen when an object can have different states: A paragraph can be aligned and centered, a font definition for the header can be set to bold and have a certain site, and so on. When it comes to the method names of the fluent interface the prefix "no" and the postfix "on"/"off" are used. "on/off" is only used as a suffix when two different methods can toggle the status. So it's "noLineNumbering()" and not "lineNumberingOff()" because you can't toggle the line numbering back. (RTF does not support this.)
  * jRTF builds an internal representation of the RTF document first and writes it later to a stream. Currently jRTF don't take advantage of that because it could for example accept a font or color definition during writing a text and insert automatically a header information which is written in the second pass.


What's not supported and how YOU can help
=========================================


jRTF is grown out of my own need to generate RTF documents. Some background about this project: http://www.tutego.de/ is a German training institute and we are generating RTF documents for two reasons. First we send a document in revision mode to every trainer on a regular basis with his/her contact details, availability and a table with his/her seminars so the trainer can make changes and additions. Secondly, the jRTF template mechanism is used to prepare offers if a customer asks for a training. Later I added things because they were just easy to add: pictures, different formattings (but does somebody double underline today?) So for me jRTF does the job and probably I will not add a lot more stuff until I need it.

So a lot of things are missing like nice table handling, bookmarks, comments/annotations, drawing objects, sub documents, all object/shape stuff, embedded fonts, GIF images, and a lot more. The current RTF 1.7 standard has too many control words (around 1400). Some of them are easy to add, some conceptual things are a little bit more work.
You can help with

  * testing jRTF
  * finding out where jRTF generates wrong RTF and where exceptions should be thrown
  * adding more control words to jRTF to support more formattings/styles
  * add more fields
  * uncomment the style support and make it work
  * make the table stuff work, introduce a `RtfCell` class
  * add GIF and WMF support. You might want to integrate http://commons.apache.org/sanselan/
  * build an enum for different languages so that the whole document, a certain paragraph or text can be in different languages
  * abstract from font, style and color positions in header and introduce names, which are mapped to these positions

Furthermore some design decisions has to be made according to formattings: The problem now is that some formattings can appear at several places. Take paragraph formattings for example. They can appear in header definitions and also "local" within the paragraph itself. I started using an `EnumSet` for this formattings which have the big advantage that a format can be reused and building is quite nice. But for consistency with the other classes I changed to the fluent interface/builder style which has the disadvantage of heavy source duplication and the lack of reusing a certain style. This problem can be solved in three different ways:

  - Continue with the builder style and bring people to using styles so that local paragraph formattings are not necessary.
  - Offer either a builder style and a `EnumSet` style.
  - Change to `EnumSet` and accept that there a two different API "styles".


Alternative Libraries
=====================

If you are looking to a mature open source alternative take a look at iText RTF (http://sourceforge.net/projects/itextrtf/). Unfortunately the RTF support was removed from the official build and you have to search for the RTF version explicitly.


Thanks for patches and help
===========================

  * Bill Stackhouse
