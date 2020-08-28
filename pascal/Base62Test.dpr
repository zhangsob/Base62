{ charset : EUC-KR }
program Base62Test;

{$APPTYPE CONSOLE}

uses
  SysUtils,
  Base62 in 'Base62.pas';

function bin2hexa(bin : TBytes) : string ;
const
  HexaString = '0123456789ABCDEF' ;
var
  i : Integer ;
begin
  result := '' ;
  for i := 0 to Length(bin)-1 do
  begin
    result := result + HexaString[1 + (bin[i] shr 4)] ;
    result := result + HexaString[1 + (bin[i] and 15)] ;
    result := result + ' ' ;

    if (i mod 16) = 7 then
      result := result + ' '
    else if (i mod 16) = 15 then
      result := result + #10 ;
  end ;
end;

var
  bin, dst : TBytes ;
  txt : string ;
  i : Integer ;

begin
  SetLength(bin, 256) ;
  for i := 0 to Length(bin)-1 do
    bin[i] := i ;

  WriteLn('---bin[', Length(bin), ']---') ;
  WriteLn(bin2hexa(bin)) ;

  txt := TBase62.Encode(bin) ;
  WriteLn('txt[', Length(txt), ']:', txt) ;

  dst := TBase62.Decode(txt) ;
  WriteLn('---dst[', Length(dst), ']---') ;
  WriteLn(bin2hexa(dst)) ;
  
  ReadLn;
end.