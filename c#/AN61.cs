using System;
using System.Text;

/// <summary>
/// AN61(AlphaNumeric61)란..Text에서 특수문자를 제거한 숫자(10)+영문대문자(26)+영문소문자(26) = 62가지 문자로 변환하기
///
/// 원리 : https://github.com/zhangsob/Base62 를 참조한다.
///
/// @author     zhangsob@gmail.com
/// 
/// @history    2020-08-26 encode(), decode() 만듦.
/// </summary>
namespace Base62
{
    class AN61
    {
        private static char[] toBase62 = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        };

        private static int[] fromBase62 = new int[128];

        private static void fill()
        {
            for (int i = 0, len = fromBase62.Length; i < len; ++i)
                fromBase62[i] = -1;
            for (int i = 0, len = toBase62.Length; i < len; i++)
                fromBase62[toBase62[i]] = i;
            fromBase62['z'] = -2;
        }

        public static string encode(string text)
        {
            byte[] utf8 = Encoding.UTF8.GetBytes(text);
            StringBuilder ret = new StringBuilder();
            int value = 0;
            int val = 0;
            int len = utf8.Length;
            char[] tmp = new char[4];
            for (int i = 0; i < len; ++i)
            {
                val = (utf8[i] & 0xFF);
                if (val >= 0xF0)
                {
                    for (int j = 0; j < text.Length; ++j)
                        if(Char.ConvertToUtf32(text, j) > 0xFFFF)
                            throw new ArgumentException("invalid UCS2 character index " + j + " " + text.Substring(j,2));
                }

                value = value * 0xF0 + val;
                if (i % 3 == 2)
                {
                    for (int j = 3; j >= 0; --j, value /= 61)
                        tmp[j] = toBase62[value % 61];

                    value = 0;
                    ret.Append(tmp, 0, 4);
                }
            }

            len = utf8.Length % 3;
            if (len > 0)
            {
                for (int j = len; j >= 0; --j, value /= 61)
                    tmp[j] = toBase62[value % 61];

                ret.Append(tmp, 0, len + 1);
            }

            return ret.ToString();
        }

        public static string decode(string text)
        {
            int len = text.Length;
            if(len % 4 == 1)    throw new ArgumentException("invalid AN61 length");

            fill();
            byte[] dst = new byte[len / 4 * 3 + ((len % 4 > 0) ? len % 4 - 1 : 0)];
            byte[] tmp = new byte[3];
            int value = 0;
            int val = 0;
            char ch = (char)0;

            int bi = 0;
            for(int i = 0; i < len; ++i) {
                ch = text[i] ;
                if(ch >= 0x80)
                    throw new ArgumentException("invalid AN61 character " + ch);

                val = fromBase62[ch] ;
                if(val< 0)
                    throw new ArgumentException("invalid AN61 character " + ch);

                value = value* 61 + val;
                if(i % 4 == 3) {
                    for(int j = 2; j >= 0; --j, value /= 0xF0)
                        tmp[j] = (byte)(value % 0xF0) ;

                    value = 0 ;
                    Array.Copy(tmp, 0, dst, bi, 3);
                    bi += 3 ;
                }
            }

            len = len % 4 ;
            if(len > 0) {
                len -= 1 ;
                for(int j = len - 1; j >= 0; --j, value /= 0xF0)
                    tmp[j] = (byte)(value % 0xF0) ;

                Array.Copy(tmp, 0, dst, bi, len);
                bi += len ;
            }
        
            return Encoding.UTF8.GetString(dst, 0, bi);
        }
    }
}
