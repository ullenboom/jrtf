/*
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jRTF' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tutego.jrtf;

/**
 * RTF control word names (without leading backslash &mdash; that is added by the
 * {@link RtfOutput} helpers). Grouped by the area of the RTF spec they belong to.
 * Compound destinations that begin with the {@code \*} control symbol keep an
 * internal backslash in their name, e.g. {@code *\listtable}.
 */
final class RtfControlWords {
  private RtfControlWords() {}

  // Character formatting

  static final String BOLD = "b";
  static final String ITALIC = "i";
  static final String UNDERLINE = "ul";
  static final String SUBSCRIPT = "sub";
  static final String SUPERSCRIPT = "super";
  static final String STRIKETHROUGH = "strike";
  static final String SHADOW = "shad";
  static final String FONT = "f";
  static final String FONT_SIZE = "fs";
  static final String FONT_CHARSET = "fcharset";
  static final String FONT_PITCH = "fprq";
  static final String CHAR_BACKGROUND_COLOR = "cb";
  static final String CHAR_FOREGROUND_COLOR = "cf";
  static final String FOOTNOTE_REF_MARK = "chftn";
  static final String FOOTNOTE_DESTINATION = "footnote";
  static final String SUPERSCRIPT_RAISE = "up";
  static final String FIELD_DIRTY = "flddirty";
  static final String FIELD_EDIT = "fldedit";
  static final String FIELD_LOCKED = "fldlock";
  static final String FIELD_PRIVATE = "fldpriv";
  static final String CAPS = "caps";
  static final String HIDDEN = "v";
  static final String KERNING = "kerning";
  static final String CHAR_EXPAND = "expndtw";
  static final String SUBSCRIPT_LOWER = "dn";
  static final String UNDERLINE_DOTTED = "uld";
  static final String UNDERLINE_DOUBLE = "uldb";
  static final String UNDERLINE_WORD = "ulw";
  static final String REVISED = "revised";
  static final String SMALL_CAPS = "scaps";

  // Paragraph formatting

  static final String DESTINATION_MARKER = "*";
  static final String PARAGRAPH_DEFAULTS = "pard";
  static final String STYLE = "s";
  static final String HYPHENATE_PARAGRAPH = "hyphpar";
  static final String IN_TABLE = "intbl";
  static final String KEEP_TOGETHER = "keep";
  static final String NO_WIDOW_CONTROL = "nowidctlpar";
  static final String KEEP_WITH_NEXT = "keepn";
  static final String OUTLINE_LEVEL = "level";
  static final String NO_LINE_NUMBERING = "noline";
  static final String PAGE_BREAK_BEFORE = "pagebb";
  static final String ALIGN_LEFT = "ql";
  static final String ALIGN_RIGHT = "qr";
  static final String ALIGN_JUSTIFIED = "qj";
  static final String ALIGN_CENTERED = "qc";
  static final String FIRST_LINE_INDENT = "fi";
  static final String LEFT_INDENT = "li";
  static final String RIGHT_INDENT = "ri";
  static final String SPACE_BEFORE = "sb";
  static final String SPACE_AFTER = "sa";
  static final String LINE_SPACING = "sl";
  static final String LINE_SPACING_MULTIPLE = "slmult";
  static final String BOTTOM_BORDER = "brdrb";
  static final String BORDER_SINGLE = "brdrs";
  static final String BORDER_DOUBLE_THICKNESS = "brdrth";
  static final String BORDER_SHADOWED = "brdrsh";
  static final String BORDER_DOUBLE = "brdrdb";
  static final String BORDER_DOTTED = "brdrdot";
  static final String BORDER_DASHED = "brdrdash";
  static final String BORDER_HAIRLINE = "brdrhair";
  static final String TAB = "tab";
  static final String PAR = "par";
  static final String BASED_ON_STYLE = "sbasedon";
  static final String RIGHT_TO_LEFT_PARAGRAPH = "rtlpar";
  static final String LEFT_TO_RIGHT_PARAGRAPH = "ltrpar";
  static final String TAB_POSITION = "tx";
  static final String BAR_TAB_POSITION = "tb";
  static final String TAB_RIGHT = "tqr";
  static final String TAB_CENTER = "tqc";
  static final String TAB_DECIMAL = "tqdec";
  static final String TAB_LEAD_DOTS = "tldot";
  static final String TAB_LEAD_HYPHENS = "tlhyph";
  static final String TAB_LEAD_UNDERLINE = "tlul";
  static final String TAB_LEAD_THICK_LINE = "tlth";
  static final String TAB_LEAD_EQUALS_SIGN = "tleq";
  static final String PARAGRAPH_BORDER_TOP = "brdrt";
  static final String PARAGRAPH_BORDER_BOTTOM = "brdrb";
  static final String PARAGRAPH_BORDER_LEFT = "brdrl";
  static final String PARAGRAPH_BORDER_RIGHT = "brdrr";
  static final String BORDER_WIDTH = "brdrw";
  static final String BORDER_COLOR = "brdrcf";
  static final String PARAGRAPH_SHADING = "shading";
  static final String PARAGRAPH_BACKGROUND_COLOR = "cbpat";

  // Paragraph numbering (list items)

  static final String PARAGRAPH_NUMBERING = "pn";
  static final String PN_LEVEL_BODY = "pnlvlbody";
  static final String PN_LEVEL_BULLET = "pnlvlblt";
  static final String PN_DECIMAL = "pndec";
  static final String PN_LOWER_LETTER = "pnlcltr";
  static final String PN_UPPER_LETTER = "pnucltr";
  static final String PN_LOWER_ROMAN = "pnlcrm";
  static final String PN_UPPER_ROMAN = "pnucrm";
  static final String PN_START = "pnstart";
  static final String PN_INDENT = "pnindent";
  static final String PN_FONT = "pnf";
  static final String PN_TEXT = "pntext";
  static final String PN_TEXT_AFTER = "pntxta";
  static final String PN_TEXT_BEFORE = "pntxtb";

  // List table (\listtable / \listoverridetable) and paragraph list references

  static final String LIST_TABLE_DESTINATION = "*\\listtable";
  static final String LIST = "list";
  static final String LIST_TEMPLATE_ID = "listtemplateid";
  static final String LIST_SIMPLE = "listsimple";
  static final String LIST_ID = "listid";
  static final String LIST_LEVEL = "listlevel";
  static final String LEVEL_NUMBER_FORMAT = "levelnfc";
  static final String LEVEL_JUSTIFICATION = "leveljc";
  static final String LEVEL_FOLLOW = "levelfollow";
  static final String LEVEL_START_AT = "levelstartat";
  static final String LEVEL_TEXT = "leveltext";
  static final String LEVEL_NUMBERS = "levelnumbers";
  static final String LIST_OVERRIDE_TABLE_DESTINATION = "*\\listoverridetable";
  static final String LIST_OVERRIDE = "listoverride";
  static final String LIST_OVERRIDE_COUNT = "listoverridecount";
  static final String LIST_OVERRIDE_INDEX = "ls";
  static final String LIST_LEVEL_INDEX = "ilvl";

  // Bookmarks

  static final String BOOKMARK_START = "*\\bkmkstart";
  static final String BOOKMARK_END = "*\\bkmkend";

  // Row/cell formatting

  static final String ROW_DEFAULTS = "trowd";
  static final String ROW_AUTOFIT = "trautofit";
  static final String ROW_RIGHT_TO_LEFT = "taprtl";
  static final String CELL_PADDING_UNIT_TOP = "trpaddft";
  static final String CELL_PADDING_UNIT_BOTTOM = "trpaddfb";
  static final String CELL_PADDING_UNIT_LEFT = "trpaddfl";
  static final String CELL_PADDING_UNIT_RIGHT = "trpaddfr";
  static final String CELL_PADDING_TOP = "trpaddt";
  static final String CELL_PADDING_BOTTOM = "trpaddb";
  static final String CELL_PADDING_LEFT = "trpaddl";
  static final String CELL_PADDING_RIGHT = "trpaddr";
  static final String CELL_BORDER_BOTTOM = "clbrdrb";
  static final String CELL_BORDER_TOP = "clbrdrt";
  static final String CELL_BORDER_LEFT = "clbrdrl";
  static final String CELL_BORDER_RIGHT = "clbrdrr";
  static final String ROW_GAP = "trgaph";
  static final String ROW_HEIGHT = "trrh";
  static final String ROW_HEADER_REPEAT = "trhdr";
  static final String CELL_BACKGROUND_COLOR = "clcbpat";
  static final String CELL_WIDTH_TYPE_FIXED = "clftsWidth3";
  static final String CELL_WIDTH = "clwWidth";
  static final String CELL_BOUNDARY = "cellx";
  static final String CELL = "cell";
  static final String ROW = "row";
  static final String CELL_MERGE_FIRST = "clmgf";
  static final String CELL_MERGE = "clmrg";
  static final String CELL_VERTICAL_MERGE_FIRST = "clvmgf";
  static final String CELL_VERTICAL_MERGE = "clvmrg";
  static final String CELL_VERTICAL_ALIGN_TOP = "clvertalt";
  static final String CELL_VERTICAL_ALIGN_CENTER = "clvertalc";
  static final String CELL_VERTICAL_ALIGN_BOTTOM = "clvertalb";

  // Document and section formatting

  static final String RTF_VERSION = "rtf";
  static final String ANSI_CHARSET = "ansi";
  static final String DEFAULT_FONT = "deff";
  static final String DEFAULT_TAB_WIDTH = "deftab";
  static final String INFO_DESTINATION = "info";
  static final String HYPHENATION_HOT_ZONE = "hyphhotzN";
  static final String VIEW_KIND = "viewkind";
  static final String FOOTNOTE_ENDNOTE_PLACEMENT = "fet";
  static final String FOOTNOTE_NUMBERING_ARABIC = "ftnnar";
  static final String FOOTNOTE_NUMBERING_UPPER_ALPHA = "ftnnauc";
  static final String FOOTNOTE_NUMBERING_UPPER_ROMAN = "ftnnrlc";
  static final String PAPER_WIDTH = "paperw";
  static final String PAPER_HEIGHT = "paperh";
  static final String MARGIN_LEFT = "margl";
  static final String MARGIN_RIGHT = "margr";
  static final String MARGIN_TOP = "margt";
  static final String MARGIN_BOTTOM = "margb";
  static final String FACING_PAGES = "facingp";
  static final String MIRROR_MARGINS = "margmirror";
  static final String LANDSCAPE = "landscape";
  static final String WIDOW_CONTROL = "widowctrl";
  static final String REVISION_PROTECTED = "revprot";
  static final String REVISION_MARKING = "revisions";
  static final String SECTION_DEFAULTS = "sectd";
  static final String END_NOTES_HERE = "endnhere";
  static final String SECTION_BREAK_NONE = "sbknone";
  static final String SECTION_BREAK_COLUMN = "sbkcol";
  static final String SECTION_BREAK_PAGE = "sbkpage";
  static final String SECTION_BREAK_EVEN = "sbkeven";
  static final String SECTION_BREAK_ODD = "sbkodd";
  static final String COLUMNS = "cols";
  static final String COLUMN_SPACE = "colsx";
  static final String LINE_BETWEEN_COLUMNS = "linebetcol";
  static final String PAGE_NUMBER_RESTART = "pgnstarts";
  static final String PAGE_NUMBER_LOWER_ROMAN = "pgnlcrm";
  static final String PAGE_NUMBER_UPPER_ROMAN = "pgnucrm";
  static final String PAGE_NUMBER_DECIMAL = "pgndec";
  static final String TITLE_PAGE = "titlepg";
  static final String VERTICAL_ALIGN_TOP = "vertalt";
  static final String VERTICAL_ALIGN_BOTTOM = "vertalb";
  static final String VERTICAL_ALIGN_CENTER = "vertalc";
  static final String VERTICAL_ALIGN_JUSTIFIED = "vertalj";
  static final String SECTION = "sect";
  static final String STYLE_SHEET = "stylesheet";
  static final String FONT_TABLE = "fonttbl";
  static final String COLOR_TABLE = "colortbl";
  static final String RED = "red";
  static final String GREEN = "green";
  static final String BLUE = "blue";

  // Special characters and destinations

  static final String UNICODE_CHAR = "u";
  static final String CURRENT_DATE = "chdate";
  static final String CURRENT_DATE_LONG = "chdpl";
  static final String CURRENT_DATE_ABBREVIATED = "chdpa";
  static final String CURRENT_TIME = "chtime";
  static final String CURRENT_PAGE_NUMBER = "chpgn";
  static final String CURRENT_SECTION_NUMBER = "sectnum";
  static final String PAGE_BREAK = "page";
  static final String COLUMN_BREAK = "column";
  static final String LINE_BREAK = "line";
  static final String SOFT_PAGE_BREAK = "softpage";
  static final String SOFT_COLUMN_BREAK = "softcol";
  static final String SOFT_LINE_BREAK = "softline";
  static final String EM_DASH = "emdash";
  static final String EN_DASH = "endash";
  static final String LEFT_SINGLE_QUOTE = "lquote";
  static final String RIGHT_SINGLE_QUOTE = "rquote";
  static final String LEFT_DOUBLE_QUOTE = "ldblquote";
  static final String RIGHT_DOUBLE_QUOTE = "rdblquote";
  static final String BULLET = "bullet";
  static final String NON_BREAKING_SPACE = "~";
  static final String PICTURE_DESTINATION = "pict";
  static final String JPEG_PICTURE = "jpegblip";
  static final String PNG_PICTURE = "pngblip";
  static final String EMF_PICTURE = "emfblip";
  static final String PICTURE_WIDTH_GOAL = "picwgoal";
  static final String PICTURE_HEIGHT_GOAL = "pichgoal";
  static final String PICTURE_SCALE_X = "picscalex";
  static final String PICTURE_SCALE_Y = "picscaley";

  // Fields

  static final String FIELD = "field";
  static final String FIELD_INSTRUCTION_DESTINATION = "*\\fldinst";
  static final String FIELD_RESULT_DESTINATION = "fldrslt";

  // Form fields

  static final String FORM_FIELD = "field";
  static final String FORM_FIELD_TEXT = "fftypetxt";
  static final String FORM_FIELD_CHECKBOX = "fftypechk";
  static final String FORM_FIELD_DROPDOWN = "fftypelst";
  static final String FORM_FIELD_NAME = "*\\ffname";
  static final String FORM_FIELD_STATUS_TEXT = "*\\ffstattext";
  static final String FORM_FIELD_HELP_TEXT = "*\\ffhelptext";
  static final String FORM_FIELD_DEFAULT = "*\\ffdeftext";
  static final String FORM_FIELD_MAX_LENGTH = "ffmaxlen";
  static final String FORM_FIELD_CHECKED = "ffchecked";
  static final String FORM_FIELD_SIZE = "ffsize";
  static final String FORM_FIELD_LIST_ITEM = "*\\ffl";

  // Annotations / comments

  static final String ANNOTATION_DESTINATION = "*\\annot";
  static final String ANNOTATION_ID = "atnid";
  static final String ANNOTATION_AUTHOR = "atnauthor";
  static final String ANNOTATION_REFERENCE_START = "atrfstart";
  static final String ANNOTATION_REFERENCE_END = "atrfend";

  // Track changes / revision

  static final String REVISION_AUTHOR = "revauth";
  static final String REVISION_DATE = "revdttm";

  // Bidirectional character-level text

  static final String RIGHT_TO_LEFT_CHAR = "rtlch";
  static final String LEFT_TO_RIGHT_CHAR = "ltrch";

  // Document info group

  static final String INFO_SUBJECT = "subject";
  static final String INFO_TITLE = "title";
  static final String INFO_AUTHOR = "author";
  static final String INFO_CREATION_TIME = "creatim";
  static final String INFO_REVISION_TIME = "revtim";
  static final String INFO_PRINT_TIME = "printim";
  static final String INFO_KEYWORDS = "keywords";
  static final String INFO_COMMENT = "comment";
  static final String INFO_OPERATOR = "operator";
  static final String INFO_DOC_COMMENT = "doccomm";
  static final String INFO_VERSION = "version";
  static final String INFO_NUMBER_OF_WORDS = "nofwords";
  static final String INFO_NUMBER_OF_PAGES = "nofpages";
  static final String INFO_YEAR = "yr";
  static final String INFO_MONTH = "mo";
  static final String INFO_DAY = "dy";
  static final String INFO_HOUR = "hr";
  static final String INFO_MINUTE = "min";
  static final String INFO_SECOND = "sec";
}
