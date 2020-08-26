#-*- coding: utf-8 -*-
import base64
from Base62 import Base62

class AN61(object) :
    """AN61(AlphaNumeric61)란.. 
    Text에서 특수문자를 제거한 숫자(10)+영문대문자(26)+영문소문자(26) = 62가지 문자로 변환하기

    원리 : https://github.com/zhangsob/Base62 를 참조한다.

    author : zhangsob@gmail.com

    history : 2020-08-27 만듦.
    """
    _toBase62 = [
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                ]

    _fromBase62 = [ -1 for i in range(128) ]
    for i in range(len(_toBase62)) :
        _fromBase62[ord(_toBase62[i])] = i
    _fromBase62[ ord('z') ] = -2

    @staticmethod
    def encode(text) :
        if not isinstance(text, str) :
            raise TypeError('encode(text is str)')
        utf8 = text.encode('utf8')
        ret = ""
        value = 0
        tmp = ['0', '0', '0', '0']
        #AN62.PrintBinary("utf8", utf8)

        for i in range(0, len(utf8)) :
            val = utf8[i]
            if(val >= 0xF0) :
                for j in range(0, len(text)) :
                    if(ord(text[j]) > 0xFFFF) :
                        raise ValueError("Illegal base62 character index " + str(j) + " " + text[j:j+2])
            
            value = value * 0xF0 + val

            if(i % 3 == 2) :
                for j in range(3, -1, -1) :
                    tmp[j] = AN61._toBase62[value % 61]
                    value = value // 61

                for j in range(0, 4) :
                    ret += tmp[j]

                value = 0
        
        length = len(utf8) % 3
        if(length > 0) :
            for j in range(length, -1, -1) : 
                tmp[j] = AN61._toBase62[value % 61]
                value = value // 61
            
            for j in range(0, length+1) :
                ret += tmp[j]
        
        return ret

    @staticmethod
    def decode(text) :
        if not isinstance(text, str) :
            raise TypeError('decode(text is str)')

        length = len(text)
        if(length % 4 == 1) :
            raise ValueError("invalid AN61 length")
        
        dst = bytearray([])
        tmp = [ 0 for i in range(4) ]
        value = 0
        for i in range(length) :
            ch = ord(text[i])
            if(ch >= 0x80) :
                raise ValueError("invalid AN61 character " + text[i])
            
            value = value * 61 + AN61._fromBase62[ch]

            if(i % 4 == 3) :
                for j in range(2, -1, -1) :
                    tmp[j] = value % 0xF0
                    value //= 0xF0

                for j in range(3) :
                    dst.append(tmp[j])
        
        length = length % 4
        if(length > 0) :
            length -= 1
            for j in range(length-1, -1, -1) :
                tmp[j] = value % 0xF0
                value //= 0xF0

            for j in range(length) :
                dst.append(tmp[j])

        return dst.decode('utf8')

if __name__ == '__main__':
    src0 = 'http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可'
    print('src0['+str(len(src0))+']:' + src0)
    an61__tmp0 = AN61.encode(src0)
    print('an61__tmp0['+str(len(an61__tmp0))+']:' + an61__tmp0)
    an61__out0 = AN61.decode(an61__tmp0)
    print('an61__out0['+str(len(an61__out0))+']:' + an61__out0)
    print('src0 == an61__out0 :', (src0 == an61__out0))
    base64_tmp = base64.b64encode(src0.encode('utf8')).decode('utf8')
    print('base64_tmp['+str(len(base64_tmp))+']:' + base64_tmp)
    base64_out = base64.b64decode(base64_tmp.encode('utf8')).decode('utf8')
    print('base64_out['+str(len(base64_out))+']:' + base64_out)

    # [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
    src1 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可🐘" # ValueError가 발생하는 경우
    print('src1['+str(len(src1))+']:' + src1)
    try :
        tmp1 = AN61.encode(src1)
        print('tmp1:' + tmp1)
        out1 = AN61.decode(tmp1)
        print('out1:' + out1)
    except ValueError as ve :
        print('Error : ' + str(ve))
        tmp2 = Base62.encode(src1.encode("utf8"))
        print('tmp2['+str(len(tmp2))+']:' + tmp2)
        out2 = Base62.decode(tmp2).decode("utf8")
        print('out2['+str(len(out2))+']:' + out2)
        print('src1 == out2 :', (src1 == out2))
