unit AN61;

interface

uses
  SysUtils;

type
  TAN61 = class
    public
      class function Encode(const s : UTF8String) : string ; overload ;
      class function Encode(const ws : WideString) : string ; overload ;
      class function Decode(const s : string) : UTF8String ;
  end;

implementation

uses
  Classes, ZString;

const
  BASE62String = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz' ;

function _AN61_Encode(const utf8 : UTF8String) : string ;
var
  tmp : string ;
  i, j, len, val, value : Integer ;
begin
  result := '' ;
  value := 0 ;
  len := Length(utf8) ;
  for i := 0 to len - 1 do
  begin
    val := Ord(utf8[1+i]) ;
    if (val >= 240) then  // 240 : 0xF0
      raise Exception.Create('invalid UCS2 Character');

    value := value * 240 + val ;
    if( i mod 3 = 2) then
      begin
        tmp := '' ;

        for j := 3 downto 0 do
        begin
          tmp := BASE62String[1 + value mod 61] + tmp ;
          value := value div 61 ;
        end ;

        value := 0 ;
        result := result + tmp ;
      end ;
  end ;

  len := len mod 3 ;
  if (len > 0) then
    begin
      tmp := '' ;

      for i := len downto 0 do
      begin
        tmp := BASE62String[1 + value mod 61] + tmp ;
        value := value div 61 ;
      end ;

      result := result + tmp ;
    end ;
end ;

class function TAN61.Encode(const s : UTF8String) : string ;
begin
  result := _AN61_Encode(s)
end;

class function TAN61.Encode(const ws : WideString) : string ;
begin
  result := _AN61_Encode(TZString.SafeUTF8Encode(ws)) ;
end;

function AN61ToValue(const s : string) : Integer ;
begin
  result := AnsiPos(s, BASE62String) - 1;
end;

class function TAN61.Decode(const s : string) : UTF8String ;
var
  i, j, len, val, value : Integer ;
  tmp : UTF8String ;
begin
  result := '' ;
  value := 0;
  len := Length(s) ;

  for i := 1 to len do
  begin
    val := AN61ToValue(s[i]) ;
    if (val < 0) then
      raise Exception.Create('invalid AN61 Character') ;

    value := value * 61 + val ;
    if(i mod 4 = 0) then
      begin
        tmp := '' ;

        for j := 2 downto 0 do
        begin
          tmp := Chr(value mod 240) + tmp ; // 240 : 0xF0
          value := value div 240 ;
        end ;

        result := result + tmp ;
        value := 0 ;
      end ;
  end ;

  len := len mod 4 ;
  if (len > 0) then
    begin
      tmp := '' ;

      for i := len - 2 downto 0 do
        begin
          tmp := Chr(value mod 240) + tmp ;
          value := value div 240 ;
        end ;
      result := result + tmp ;
    end ;
end ;

end.

