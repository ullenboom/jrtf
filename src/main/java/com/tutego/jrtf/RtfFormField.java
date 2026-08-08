/*
 * Copyright (c) 2010-2026 Christian Ullenboom
 * All rights reserved.
 *
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

import org.jspecify.annotations.Nullable;

/**
 * Represents an RTF form field: a text input, checkbox, or dropdown list
 * embedded in a document. Use the static factory methods to create instances,
 * then insert them with {@link RtfText#formField(RtfFormField)}.
 *
 * <pre>{@code
 * RtfText.formField( RtfFormField.text().defaultText("Enter name").build() )
 * RtfText.formField( RtfFormField.checkbox().checked(true).build() )
 * RtfText.formField( RtfFormField.dropdown().item("Red").item("Green").build() )
 * }</pre>
 */
public final class RtfFormField {

  /** Field type constant. */
  @SuppressWarnings("unused") private final boolean isText, isCheckbox, isDropdown;

  /** Form field name (bookmark). */
  @Nullable private final String name;

  /** Help text shown in status bar. */
  @Nullable private final String statusText;

  /** Help text shown in F1 popup. */
  @Nullable private final String helpText;

  // ---- Text field properties ----
  @Nullable private final String defaultText;
  private final int maxLength;

  // ---- Checkbox properties ----
  private final boolean checked;
  private final int checkedSize;  // in half-points

  // ---- Dropdown properties ----
  @Nullable private final String[] items;

  private RtfFormField( boolean isText, boolean isCheckbox, boolean isDropdown,
                        @Nullable String name, @Nullable String statusText,
                        @Nullable String helpText, @Nullable String defaultText,
                        int maxLength, boolean checked, int checkedSize,
                        @Nullable String[] items ) {
    this.isText = isText;
    this.isCheckbox = isCheckbox;
    this.isDropdown = isDropdown;
    this.name = name;
    this.statusText = statusText;
    this.helpText = helpText;
    this.defaultText = defaultText;
    this.maxLength = maxLength;
    this.checked = checked;
    this.checkedSize = checkedSize;
    this.items = items;
  }

  // ---- Builders ----

  /** Creates a builder for a text-input form field. */
  public static TextBuilder text() { return new TextBuilder(); }

  /** Creates a builder for a checkbox form field. */
  public static CheckboxBuilder checkbox() { return new CheckboxBuilder(); }

  /** Creates a builder for a dropdown-list form field. */
  public static DropdownBuilder dropdown() { return new DropdownBuilder(); }

  // ---- RTF rendering ----

  void rtf( RtfOutput out ) {
    out.open().cw( RtfControlWords.FORM_FIELD );
    if ( isText )
      out.cw( RtfControlWords.FORM_FIELD_TEXT );
    else if ( isCheckbox )
      out.cw( RtfControlWords.FORM_FIELD_CHECKBOX );
    else
      out.cw( RtfControlWords.FORM_FIELD_DROPDOWN );

    if ( name != null )
      out.open( RtfControlWords.FORM_FIELD_NAME ).sp()
         .append( Rtf.asRtf( name ) ).close();

    if ( statusText != null )
      out.open( RtfControlWords.FORM_FIELD_STATUS_TEXT ).sp()
         .append( Rtf.asRtf( statusText ) ).close();

    if ( helpText != null )
      out.open( RtfControlWords.FORM_FIELD_HELP_TEXT ).sp()
         .append( Rtf.asRtf( helpText ) ).close();

    if ( isText ) {
      if ( defaultText != null )
        out.open( RtfControlWords.FORM_FIELD_DEFAULT ).sp()
           .append( Rtf.asRtf( defaultText ) ).close();
      if ( maxLength > 0 )
        out.cw( RtfControlWords.FORM_FIELD_MAX_LENGTH ).append( maxLength );
    }
    else if ( isCheckbox ) {
      out.cw( RtfControlWords.FORM_FIELD_CHECKED ).append( checked ? "1" : "0" )
         .cw( RtfControlWords.FORM_FIELD_SIZE, checkedSize );
    }
    else {
      if ( items != null ) {
        for ( String item : items ) {
          if ( item == null ) continue;
          out.open( RtfControlWords.FORM_FIELD_LIST_ITEM ).sp()
             .append( Rtf.asRtf( item ) ).close();
        }
      }
    }

    out.close();
  }

  // ---- Builder classes ----

  /** Builder for a text-input form field. */
  public static final class TextBuilder {
    private String name = null;
    private String statusText = null;
    private String helpText = null;
    private String defaultText = null;
    private int maxLength = 0;

    public TextBuilder name( String name ) { this.name = name; return this; }
    public TextBuilder statusText( String text ) { this.statusText = text; return this; }
    public TextBuilder helpText( String text ) { this.helpText = text; return this; }
    public TextBuilder defaultText( String text ) { this.defaultText = text; return this; }
    public TextBuilder maxLength( int maxLength ) { this.maxLength = maxLength; return this; }
    public RtfFormField build() { return new RtfFormField( true, false, false, name, statusText, helpText, defaultText, maxLength, false, 0, null ); }
  }

  /** Builder for a checkbox form field. */
  public static final class CheckboxBuilder {
    private String name = null;
    private String statusText = null;
    private String helpText = null;
    private boolean checked = false;
    private int checkedSize = 20;

    public CheckboxBuilder name( String name ) { this.name = name; return this; }
    public CheckboxBuilder statusText( String text ) { this.statusText = text; return this; }
    public CheckboxBuilder helpText( String text ) { this.helpText = text; return this; }
    public CheckboxBuilder checked( boolean checked ) { this.checked = checked; return this; }
    public CheckboxBuilder size( int halfPoints ) { this.checkedSize = halfPoints; return this; }
    public RtfFormField build() { return new RtfFormField( false, true, false, name, statusText, helpText, null, 0, checked, checkedSize, null ); }
  }

  /** Builder for a dropdown-list form field. */
  public static final class DropdownBuilder {
    private String name = null;
    private String statusText = null;
    private String helpText = null;
    private java.util.ArrayList<String> items = new java.util.ArrayList<>();

    public DropdownBuilder name( String name ) { this.name = name; return this; }
    public DropdownBuilder statusText( String text ) { this.statusText = text; return this; }
    public DropdownBuilder helpText( String text ) { this.helpText = text; return this; }
    public DropdownBuilder item( String item ) { items.add( item ); return this; }
    public RtfFormField build() { return new RtfFormField( false, false, true, name, statusText, helpText, null, 0, false, 0, items.toArray( new String[ 0 ] ) ); }
  }
}
