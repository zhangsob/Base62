unit Base62;

interface

type
  TBytes = array of byte ;
  
  TBase62 = class
    public
      class function Encode(const bs : array of byte) : string ;
      class function Decode(const s : string) : TBytes ;
  end;

implementation
uses
  SysUtils ;

const
  BASE62String = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz' ;

class function TBase62.Encode(const bs : array of byte) : string ;
var
  tmp : string ;
  i, j, len, value, FX_bit : Integer ;
begin
  result := '' ;
  len := Length(bs) div 3 * 3;
  i := 0 ;
  while i < len do
  begin
    value := 0 ;
    FX_bit := 0 ;

    for j := 0 to 2 do
    begin
       FX_bit := FX_bit shl 1 ;
       value := value shl 4 ;
       if (bs[i+j] and 240) = 240 then
         FX_bit := FX_bit or 1
       else
         begin
           value := value + ((bs[i+j] shr 4) and 15) ;
           value := value shl 4 ;
         end;
       value := value or (bs[i+j] and 15) ;
    end ;

    if (FX_bit > 0) then
      begin
       result := result + 'z' ;
       value := value or (FX_bit shl 20) ;
      end
    else
      begin
        value := 0 ;
        for j := 0 to 2 do
          value := value * 240 + bs[i+j] ;
      end;

    tmp := '' ;

    for j := 3 downto 0 do
    begin
      tmp := BASE62String[1 + value mod 61] + tmp ;
      value := value div 61 ;
    end ;

    result := result + tmp ;
    i := i + 3 ;
  end ;

  len := Length(bs) mod 3 ;
  if (len > 0) then
  begin
    value := 0 ;
    FX_bit := 0 ;

    for j := 0 to len-1 do
    begin
       FX_bit := FX_bit shl 1 ;
       value := value shl 4 ;
       if (bs[i+j] and 240) = 240 then
         FX_bit := FX_bit or 1
       else
         begin
           value := value + ((bs[i+j] shr 4) and 15) ;
           value := value shl 4 ;
         end;
       value := value or (bs[i+j] and 15) ;
    end ;

    if (FX_bit > 0) then
      begin
        result := result + 'z' ;

        if (len = 1) then
          value := value or (FX_bit shl 4)
        else
          value := value or (FX_bit shl 12) ;
      end
    else
      begin
        value := 0 ;
        for j := 0 to len-1 do
          value := value * 240 + bs[i+j] ;
      end;

    tmp := '' ;
    
    for j := len downto 0 do
    begin
      tmp := BASE62String[1 + value mod 61] + tmp ;
      value := value div 61 ;
    end ;

    result := result + tmp ;
  end ;
end ;

function Base62ToValue(const s : string) : Integer ;
begin
  result := AnsiPos(s, BASE62String) - 1;
end;

class function TBase62.Decode(const s : string) : TBytes ;
var
  i, j, len, val, value, count, isFx, mask, r : Integer ;
begin
  value := 0 ;
  count := 0 ;
  len := Length(s) ;
  r := 0 ;
  isFX := 0 ;
  SetLength(result, len) ;

  for i := 0 to len - 1 do
    begin
      val := Base62ToValue(s[1+i]) ;
      if (val < 0) then
        raise Exception.Create('invalid Base62 Character') ;

      if (val = 61) then
        if (isFX = 0) and ((count mod 4) = 0) then
          begin
            isFX := 1 ;
            continue ;
          end
        else
          raise Exception.Create('invalid Base62 Characters') ;

      count := count + 1 ;

      value := value * 61 + val ;
      if (count mod 4) = 0 then
        begin
          if (isFX > 0) then
            begin
              isFX := value shr 20 ;
              mask := 1 ;
              for j := 2 downto 0 do
              begin
                val := (value and 15) ;
                value := value shr 4 ;

                if (isFx and mask) = mask then
                  result[r + j] := Byte(val or 240)
                else
                  begin
                    result[r + j] := Byte(val or ((value and 15) shl 4)) ;
                    value := value shr 4 ;
                  end ;

                mask := mask shl 1 ;
              end ;
              isFx := 0 ;
            end
          else
            for j := 2 downto 0 do
            begin
              result[r + j] := Byte(value mod 240) ; // 240 : 0xF0
              value := value div 240 ;
            end ;

          r := r + 3 ;
          value := 0 ;
        end ;
    end ;

    len := count mod 4 ;
    if (len > 0) then
      begin
        len := len - 1 ;

        if (isFX > 0) then
          begin
            if (len = 2) then
              isFX := value shr 12
            else
              isFX := value shr 4 ;

            mask := 1 ;
            for j := len-1 downto 0 do
            begin
              val := (value and 15) ;
              value := value shr 4 ;

              if (isFx and mask) = mask then
                result[r + j] := Byte(val or 240)
              else
                begin
                  result[r + j] := Byte((val and 15) shl 4) ;
                  value := value shr 4 ;
                end ;

              mask := mask shl 1 ;
            end ;
          end
        else
          for j := len-1 downto 0 do
          begin
            result[r + j] := Byte(value mod 240) ; // 240 : 0xF0
            value := value div 240 ;
          end ;
        r := r + len ;
      end ;
    SetLength(result, r) ;
end;

end.
