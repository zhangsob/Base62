{ charset : EUC-KR }
program AN61Test;

{$APPTYPE CONSOLE}

uses
  SysUtils,
  AN61 in 'AN61.pas',
  Base62 in 'Base62.pas',
  ZString in 'ZString.pas';

var
  wsrc, wout : WideString ;
  wtmp, tmp0, tmp8 : string ;
  src0, out0 : AnsiString ;
  utf8, out8 : UTF8String ;
  
begin
  WriteLn('----------AnsiString------------') ;
  src0 := 'http://test.com:8080/an61.do?name=°¡³ª´Ù ¤¡¤¤¡Ø'#10'Ê¦' ;  // Ansi
  WriteLn('src0[', Length(src0), ']:', src0) ;

  tmp0 := TAN61.Encode(AnsiToUtf8(src0)) ;
  WriteLn('tmp0[', Length(tmp0), ']:', tmp0) ;

  out0 := Utf8ToAnsi(TAN61.Decode(tmp0)) ;
  WriteLn('out0[', Length(out0), ']:', out0) ;

  WriteLn('src0 = out0 : ', (src0 = out0)) ;

  WriteLn('----------UTF8String----------') ;
  utf8 := AnsiToUtf8('http://test.com:8080/an61.do?name=°¡³ª´Ù ¤¡¤¤¡Ø'#10'Ê¦') ;
  WriteLn('utf8[', Length(utf8), ']:', Utf8ToAnsi(utf8)) ;

  tmp8 := TAN61.Encode(utf8) ;
  WriteLn('tmp8[', Length(tmp8), ']:', tmp8) ;

  out8 := TAN61.Decode(tmp8) ;
  WriteLn('out8[', Length(out8), ']:', Utf8ToAnsi(out8)) ;

  WriteLn('utf8 = out8 : ', (utf8 = out8)) ;

  WriteLn('----------WideString----------') ;
  wsrc := 'http://test.com:8080/an61.do?name=°¡³ª´Ù ¤¡¤¤¡Ø'#10'Ê¦' ;
  WriteLn('wsrc[', Length(wsrc), ']:', wsrc) ;

  wtmp := TAN61.Encode(wsrc) ;
  WriteLn('wtmp[', Length(wtmp), ']:', wtmp) ;

  wout := UTF8Decode(TAN61.Decode(wtmp)) ;
  WriteLn('wout[', Length(wout), ']:', wout) ;

  WriteLn('wsrc = wout : ', (wsrc = wout)) ;

  Writeln('----------UTF8String----------') ;
  // [ ÄÚ³¢¸® = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
  utf8 := UTF8Encode('http://test.com:8080/an61.do?name=°¡³ª´Ù ¤¡¤¤¡Ø'#10'Ê¦') ;
  utf8 := utf8 + #240 ; // 0xF0 #240
  utf8 := utf8 + #159 ; // 0x9F #159
  utf8 := utf8 + #144 ; // 0x90 #144
  utf8 := utf8 + #152 ; // 0x98 #152
  //WriteLn('utf8[', Length(utf8), ']:', UTF8Decode(utf8)) ; // ÄÚ³¢¸® ¶§¹®¿¡ ¾ÈµÊ.
  WriteLn('utf8[', Length(utf8), ']:', TZString.SafeUTF8Decode(utf8)) ;

  try
    tmp8 := TAN61.Encode(utf8) ;
    WriteLn('tmp8[', Length(tmp8), ']:', tmp8) ;

    out8 := TAN61.Decode(tmp8) ;
    //WriteLn('utf8[', Length(out8), ']:', UTF8Decode(out8)) ; // ÄÚ³¢¸® ¶§¹®¿¡ ¾ÈµÊ.
    WriteLn('utf8[', Length(out8), ']:', TZString.SafeUTF8Decode(out8)) ;
  except
    on e: Exception do
    begin
      WriteLn('Error :', e.Message) ;

      tmp8 := TBase62.Encode(TZString.Utf8ToBytes(utf8)) ;
      Writeln('tmp8[', Length(tmp8), ']:', tmp8) ;

      out8 := TZString.BytesToUtf8(TBase62.Decode(tmp8)) ;
      //WriteLn('utf8[', Length(out8), ']:', UTF8Decode(out8)) ; // ÄÚ³¢¸® ¶§¹®¿¡ ¾ÈµÊ.
      WriteLn('out8[', Length(out8), ']:', TZString.SafeUTF8Decode(out8)) ;

      WriteLn('out8 = out8 : ', (out8 = out8)) ;
    end ;
  end ;

  WriteLn('----------WideString------------') ;
  // [ ÄÚ³¢¸® = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
  wsrc := 'http://test.com:8080/an61.do?name=°¡³ª´Ù ¤¡¤¤¡Ø'#10'Ê¦' ;
  wsrc := wsrc + #55357 ; // 0xD83D #55357
  wsrc := wsrc + #56344 ; // 0xDC18 #56344
  WriteLn('wsrc[', Length(wsrc), ']:', wsrc) ;

  try
    wtmp := TAN61.Encode(wsrc) ;
    WriteLn('wtmp[', Length(wtmp), ']:', wtmp) ;

    wout := UTF8Decode(TAN61.Decode(wtmp)) ;
    WriteLn('wout[', Length(wout), ']:', wout) ;
  except
    on e: Exception do
    begin
      WriteLn('Error :', e.Message) ;

      //wtmp := TBase62.Encode(TZString.StringToBytes(UTF8Encode(wsrc))) ;  // ÄÚ³¢¸® ¶§¹®¿¡ ¾ÈµÊ.
      wtmp := TBase62.Encode(TZString.Utf8ToBytes(TZString.SafeUTF8Encode(wsrc))) ;
      WriteLn('wtmp[', Length(wtmp), ']:', wtmp) ;

      //wout := UTF8Decode(TZString.BytesToString(TBase62.Decode(wtmp))) ;  // ÄÚ³¢¸® ¶§¹®¿¡ ¾ÈµÊ.
      wout := TZString.SafeUTF8Decode(TZString.BytesToUtf8(TBase62.Decode(wtmp))) ;
      WriteLn('wout[', Length(wout), ']:', wout) ;

      WriteLn('wsrc = wout : ', (wsrc = wout)) ;
    end ;
  end ;

  ReadLn;
end.
