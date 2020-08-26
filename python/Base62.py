#-*- coding: utf-8 -*-
import base64

class Base62(object) :
    """Base62란.. 
    Text에서 특수문자를 제거한 숫자(10)+영문대문자(26)+영문소문자(26) = 62가지 문자로 변환하기

    원리 : https://github.com/zhangsob/Base62 참조

    author : zhangsob@gmail.com

    history : 2020-08-26 만듦.
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
    def bin2hexa(bytes_data) :
        if (type(bytes_data) is str) :
            data = bytes_data.encode()
        elif bytes_data is None :
            data = bytearray([])
        else :
            data = bytearray(bytes_data)

        len_data = len(data)
        line = ''
        for i in range(len_data) :    
            line += ('%02X ' % data[i])
            if (i % 16 == 7) :
                line += ' '
            elif (i % 16 == 15) :
                line += '\n'

        if (len_data % 16 != 0) :
            line += '\n'

        return line ;

    @staticmethod
    def encode(bin) :
        if not (isinstance(bin, bytes) or isinstance(bin, bytearray)) :
            raise TypeError('encode(bin is bytes or bytearray)')

        ret = ''
        value = 0
        length = (len(bin) // 3) * 3
        tmp = ['','','','']
        FX_bit = 0

        for i in range(0, length, 3) :
            FX_bit = 0
            value = 0
            
            for j in range(3) :
                if((bin[i+j] & 0xF0) == 0xF0) :
                    FX_bit = 1
                    break

            if (FX_bit != 0) :
                ret += 'z'

                FX_bit = 0
                for j in range(3) :
                    FX_bit <<= 1
                    if((bin[i+j] & 0xF0) == 0xF0) :
                        FX_bit |= 0x01 
                    else :
                        value <<= 4
                        value |= (bin[i+j] >> 4) & 0x0F

                    value <<= 4
                    value |= bin[i+j] & 0x0F

                value |= FX_bit << 20
            else :
                for j in range(3) : 
                    value = value * 0xF0 + (bin[i+j] & 0xFF)

            for j in range(3, -1, -1) :
                tmp[j] = Base62._toBase62[value % 61]
                value //= 61
                
            for j in range(4) :
                ret += tmp[j]

        i = length
        length = len(bin) % 3
        if(length > 0) :
            FX_bit = 0
            value = 0
            for j in range(length) :
                if((bin[i+j] & 0xF0) == 0xF0) :
                    FX_bit = 1
                    break

            if (FX_bit != 0) :
                ret += 'z'

                FX_bit = 0
                for j in range(length) :
                    FX_bit <<= 1
                    if((bin[i+j] & 0xF0) == 0xF0) :
                        FX_bit |= 0x01
                    else :
                        value <<= 4
                        value |= (bin[i+j] >> 4) & 0x0F

                    value <<= 4
                    value |= bin[i+j] & 0x0F

                value |= FX_bit << (4 if(length == 1) else 12)
            else :
                for j in range(length) :
                    value = value * 0xF0 + (bin[i+j] & 0xFF)

            for j in range(length, -1, -1) : 
                tmp[j] = Base62._toBase62[value % 61]
                value //= 61

            for j in range(length+1) :
                ret += tmp[j]
            
        return ret

    @staticmethod
    def decode(txt) :
        if not isinstance(txt, str) :
            raise TypeError('decode(txt is str)')

        length = len(txt)
        dst = bytearray([])
        tmp = [ 0 for i in range(4) ]
        value = 0
        count = 0
        isFX = 0

        for i in range(length) : 
            ch = ord(txt[i])
            if(ch >= 0x80) :
                raise ValueError("invalid Base62 character " + chr(ch))
                
            val = Base62._fromBase62[ch]
            if(val < 0) :
                if(val == -2 and (count % 4) == 0 and isFX == 0) :
                    isFX = 1
                    continue

                raise ValueError("invalid Base62 character " + chr(ch))

            count += 1

            value = value * 61 + val
            if(count % 4 == 0) :
                if (isFX != 0) :
                    isFX = value >> 20
                    mask = 1
                    for j in range(2, -1, -1) :
                        tmp[j] = value & 0x0F
                        value >>= 4
                        if ((isFX & mask) == mask) :
                            tmp[j] |= 0xF0
                        else :
                            tmp[j] |= (value & 0x0F) << 4
                            value >>= 4
                        mask <<= 1
                    isFX = 0
                else :
                    for j in range(2, -1, -1) :
                        tmp[j] = value % 0xF0
                        value //= 0xF0

                value = 0
                for j in range(0, 3) :
                    dst.append(tmp[j])

        length = count % 4
        if(length > 0) :
            length -= 1
            if (isFX != 0) :
                isFX = value >> ( 12 if (length >= 2) else 4)
                mask = 1
                for j in range(length-1, -1, -1) :
                    tmp[j] = value & 0x0F
                    value >>= 4
                    if ((isFX & mask) == mask) :
                        tmp[j] |= 0xF0
                    else :
                        tmp[j] |= (value & 0x0F) << 4
                        value >>= 4
                    mask <<= 1
            else :
                for j in range(length-1, -1, -1) :
                    tmp[j] = value % 0xF0
                    value //= 0xF0

            for j in range(length) :
                dst.append(tmp[j])

        return dst

if __name__ == '__main__':
    bin = bytearray(256) ;
    for i in range(len(bin)) :
        bin[i] = i

    print('----bin['+str(len(bin))+']----')
    print(Base62.bin2hexa(bin))
    txt = Base62.encode(bin)
    print('txt['+str(len(txt))+']:'+txt)
    out = Base62.decode(txt)
    print('----out['+str(len(out))+']----')
    print(Base62.bin2hexa(out))
    