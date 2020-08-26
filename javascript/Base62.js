/**
 * Base62란.. Text에서 특수문자를 제거한 숫자(10)+영문대문자(26)+영문소문자(26) = 62가지 문자로 변환하기
 *        
 * 원리 : https://github.com/zhangsob/Base62 를 참조한다.
 *        
 * @author zhangsob@gmail.com
 *
 * @history 2020-08-26 encode(), decode() 만듦.
 */
var Base62 = (function() {

    function num2hex(num, len) {
        var i = 0, str = '', hex_tab = '0123456789ABCDEF';
        for(i = 0; i < len; ++i) {
            str = hex_tab.charAt(num & 0x0F) + str;
            //num >>= 4 ;   // 32bit 미만에서만 사용가능
            num /= 16 ;     // 32bit 초과시에도 사용가능
        }
        return str;
    }

    function bin2hexa(bin) {
        var line = '', i = 0, len = bin.length ;
        for(; i < len; ++i) {
            line += num2hex(bin[i], 2) + ' ' ;
            if(i % 16 == 7)     line += ' ' ;
            if(i % 16 == 15)    line += '\n' ;
        }
        return (len % 16 != 0) ? line + '\n' : line ;
    }

    if (!String.prototype.codePointAt) {
        String.prototype.codePointAt = function (idx) {
            idx = idx || 0;
            var code = this.charCodeAt(idx), low = 0 ;
            if (0xD800 <= code && code <= 0xDBFF) {
                low = this.charCodeAt(idx+1) ;
                if (isNaN(low))
                    throw 'High surrogate not followed by low surrogate in codePointAt()';
                code = ((code - 0xD800) * 0x400) + (low - 0xDC00) + 0x10000;
            }
            return code ;
        };
    }

    if (!String.fromCodePoint) (function(stringFromCharCode) {
        var fromCodePoint = function(_) {
          var codeUnits = [], codeLen = 0, result = "";
          for (var index=0, len = arguments.length; index !== len; ++index) {
            var codePoint = +arguments[index];
            // correctly handles all cases including `NaN`, `-Infinity`, `+Infinity`
            // The surrounding `!(...)` is required to correctly handle `NaN` cases
            // The (codePoint>>>0) === codePoint clause handles decimals and negatives
            if (!(codePoint < 0x10FFFF && (codePoint>>>0) === codePoint))
              throw RangeError("Invalid code point: " + codePoint);
            if (codePoint <= 0xFFFF) { // BMP code point
              codeLen = codeUnits.push(codePoint);
            } else { // Astral code point; split in surrogate halves
              // https://mathiasbynens.be/notes/javascript-encoding#surrogate-formulae
              codePoint -= 0x10000;
              codeLen = codeUnits.push(
                (codePoint >> 10) + 0xD800,  // highSurrogate
                (codePoint % 0x400) + 0xDC00 // lowSurrogate
              );
            }
            if (codeLen >= 0x3fff) {
              result += stringFromCharCode.apply(null, codeUnits);
              codeUnits.length = 0;
            }
          }
          return result + stringFromCharCode.apply(null, codeUnits);
        };
        try { // IE 8 only supports `Object.defineProperty` on DOM elements
          Object.defineProperty(String, "fromCodePoint", {
            "value": fromCodePoint, "configurable": true, "writable": true
          });
        } catch(e) {
          String.fromCodePoint = fromCodePoint;
        }
    }(String.fromCharCode));
    
    var toBase62 = [
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    ];
    
    var fromBase62 = Array(128) ;
    for (i = 0; i < fromBase62.length; ++i)
        fromBase62[i] = -1 ;
    for (i = 0; i < toBase62.length; ++i)
        fromBase62[toBase62[i].charCodeAt(0)] = i ;
    fromBase62['z'.charCodeAt(0)] = -2;
        
    var encode = function(bin) {
        var ret = [] ;
        var value = 0 ;
        var len = Math.floor(bin.length / 3) * 3 ;
        var tmp = [] ;
        var FX_bit = 0 ;
        var i = 0, j = 0 ;

        for(i = 0; i < len; i += 3) {
            FX_bit = 0 ;
            value = 0 ;
            
            for(j = 0; j < 3; ++j) {
                if((bin[i+j] & 0xF0) == 0xF0) {
                    FX_bit = 1 ;
                    break ;
                }
            }

            if (FX_bit != 0) {
                ret.push('z') ;

                FX_bit = 0 ;
                for(j = 0; j < 3; ++j) {
                    FX_bit <<= 1 ;
                    if((bin[i+j] & 0xF0) == 0xF0) {
                        FX_bit |= 0x01 ;
                    }
                    else {
                        value <<= 4 ;
                        value |= (bin[i+j] >> 4) & 0x0F ;
                    }
                    value <<= 4 ;
                    value |= bin[i+j] & 0x0F ;
                }
                value |= FX_bit << 20 ;
            }
            else {
                for(j = 0; j < 3; ++j)
                    value = value * 0xF0 + (bin[i+j] & 0xFF) ;
            }

            for(j = 3; j >= 0; --j, value = Math.floor(value / 61))
                tmp[j] = toBase62[value % 61] ;
                
            for(j = 0; j <= 3; ++j)
                ret.push(tmp[j]) ;
        }
        
        len = bin.length % 3 ;
        if(len > 0) {
            FX_bit = 0 ;
            value = 0 ;
            for(j = 0; j < len; ++j) {
                if((bin[i+j] & 0xF0) == 0xF0) {
                    FX_bit = 1 ;
                    break ;
                }
            }

            if (FX_bit != 0) {
                ret.push('z') ;

                FX_bit = 0 ;
                for(j = 0; j < len; ++j) {
                    FX_bit <<= 1 ;
                    if((bin[i+j] & 0xF0) == 0xF0) {
                        FX_bit |= 0x01 ;
                    }
                    else {
                        value <<= 4 ;
                        value |= (bin[i+j] >> 4) & 0x0F ;
                    }
                    value <<= 4 ;
                    value |= bin[i+j] & 0x0F ;
                }
                value |= FX_bit << ((len == 1) ? 4 : 12) ;
            }
            else {
                for(j = 0; j < len; ++j)
                    value = value * 0xF0 + (bin[i+j] & 0xFF) ;
            }

            for(j = len; j >= 0; --j, value = Math.floor(value / 61))
                tmp[j] = toBase62[value % 61] ;

            for(j = 0; j <= len; ++j)
                ret.push(tmp[j]) ;
        }
            
        return ret.join('') ;
    }
    
    var decode = function(txt) {
        var len = txt.length ;
        var dst = [] ;
        var tmp = [] ;
        var value = 0 ;
        var val = 0 ;
        var ch = 0 ;
        var count = 0 ;    
        var bi = 0 ;
        var isFX = 0 ;
        var i = 0, j = 0 ;
        for(i = 0; i < len; ++i) {
            ch = txt.charCodeAt(i) ;
            if(ch >= 0x80)
                throw "invalid base62 character " + ch ;
                
            val = fromBase62[ch] ;
            if(val < 0) {
                if(val == -2 && (count % 4) == 0 && isFX == 0) {
                    isFX = 1 ;
                    continue ;
                }

                throw "invalid base62 character " + ch ;
            }
            ++count ;

            value = value * 61 + val ;
            if(count % 4 == 0) {
                if (isFX != 0) {
                    isFX = value >> 20 ;

                    for (j = 2, mask = 1; j >= 0; --j, mask <<= 1) {
                        tmp[j] = value & 0x0F ;
                        value >>= 4 ;
                        if ((isFX & mask) == mask) {
                            tmp[j] |= 0xF0 ;
                        }
                        else {
                            tmp[j] |= (value & 0x0F) << 4 ;
                            value >>= 4 ;
                        }
                    }
                    isFX = 0 ;
                }
                else {
                    for(j = 2; j >= 0; --j, value = Math.floor(value / 0xF0))
                        tmp[j] = value % 0xF0 ;
                }

                value = 0 ;
                for(j = 0; j < 3; ++j)
                    dst[bi++] = tmp[j] ;    
            }
        }

        len = count % 4 ;
        if(len > 0) {
            len -= 1 ;
            if (isFX != 0) {
                isFX = value >> ((len >= 2) ? 12 : 4) ;
                for (j = len-1, mask=1; j >= 0; --j, mask <<= 1) {
                    tmp[j] = value & 0x0F ;
                    value >>= 4 ;
                    if ((isFX & mask) == mask) {
                        tmp[j] |= 0xF0 ;
                    }
                    else {
                        tmp[j] |= (value & 0x0F) << 4 ;
                        value >>= 4 ;
                    }
                }
            }
            else {
                for(j = len-1; j >= 0; --j, value = Math.floor(value / 0xF0))
                    tmp[j] = value % 0xF0 ;
            }

            for(j = 0; j < len; ++j)
                dst[bi++] = tmp[j] ;
        }

        return dst ;
    }
    
    return {
        encode : encode,
        decode : decode,
        bin2hexa : bin2hexa,
    } ;
}()) ;

function print(msg) {
    if(typeof document !== 'undefined' && typeof msg === 'string')
        document.write(msg.replace(/\n/g,'<br/>') + '<br/>') ;
    console.log(msg) ;
}

try {
    var bin = [], i = 0 ;
    for(i = 0;i < 256; ++i)
        bin[i] = i ;

    print('----bin['+bin.length+']----') ;
    print(Base62.bin2hexa(bin)) ;

    var txt = Base62.encode(bin) ;
    print('txt['+txt.length+']:' + txt) ;

    var out = Base62.decode(txt) ;
    print('----out['+out.length+']----') ;
    print(Base62.bin2hexa(out)) ;
} catch(e) {
    print(e) ;
}