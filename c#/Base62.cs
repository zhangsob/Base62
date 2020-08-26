using System;
using System.Text;

/// <summary>
/// BASE62란..Text에서 특수문자를 제거한 숫자(10)+영문대문자(26)+영문소문자(26) = 62가지 문자로 변환하기
///
/// 원리 : https://github.com/zhangsob/Base62 를 참조한다.
///
/// @author     zhangsob@gmail.com
/// 
/// @history    2020-08-26 encode(), decode() 만듦.
/// </summary>
namespace Base62
{
    class Base62
    {
        private static char[] toBase62 = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        };

        private static int[] fromBase62 = new int[128];

        private static void fill() {
            for (int i = 0, len = fromBase62.Length; i < len; ++i)
                fromBase62[i] = -1;
            for (int i = 0, len = toBase62.Length; i<len; i++)
                fromBase62[toBase62[i]] = i;
            fromBase62['z'] = -2;
        }

        public static string encode(byte[] bin)
        {
            StringBuilder ret = new StringBuilder();
            int value = 0;
            int len = bin.Length / 3 * 3;
            char[] tmp = new char[4];
            int FX_bit = 0;
            int i = 0;

            for (i = 0; i < len; i += 3)
            {
                FX_bit = 0;
                value = 0;

                for (int j = 0; j < 3; ++j)
                {
                    if ((bin[i + j] & 0xF0) == 0xF0)
                    {
                        FX_bit = 1;
                        break;
                    }
                }

                if (FX_bit != 0)
                {
                    ret.Append('z');

                    FX_bit = 0;
                    for (int j = 0; j < 3; ++j)
                    {
                        FX_bit <<= 1;
                        if ((bin[i + j] & 0xF0) == 0xF0)
                        {
                            FX_bit |= 0x01;
                        }
                        else
                        {
                            value <<= 4;
                            value |= (bin[i + j] >> 4) & 0x0F;
                        }
                        value <<= 4;
                        value |= bin[i + j] & 0x0F;
                    }
                    value |= FX_bit << 20;
                }
                else
                {
                    for (int j = 0; j < 3; ++j)
                        value = value * 0xF0 + (bin[i + j] & 0xFF);
                }

                for (int j = 3; j >= 0; --j, value /= 61)
                    tmp[j] = toBase62[value % 61];

                ret.Append(tmp, 0, 4);
            }

            len = bin.Length % 3;
            if (len > 0)
            {
                FX_bit = 0;
                value = 0;
                for (int j = 0; j < len; ++j)
                {
                    if ((bin[i + j] & 0xF0) == 0xF0)
                    {
                        FX_bit = 1;
                        break;
                    }
                }

                if (FX_bit != 0)
                {
                    ret.Append('z');

                    FX_bit = 0;
                    for (int j = 0; j < len; ++j)
                    {
                        FX_bit <<= 1;

                        if ((bin[i + j] & 0xF0) == 0xF0)
                        {
                            FX_bit |= 0x01;
                        }
                        else
                        {
                            value <<= 4;
                            value |= (bin[i + j] >> 4) & 0x0F;
                        }
                        value <<= 4;
                        value |= bin[i + j] & 0x0F;
                    }

                    value |= FX_bit << ((len == 1) ? 4 : 12);
                }
                else
                {
                    for (int j = 0; j < len; ++j)
                        value = value * 0xF0 + (bin[i + j] & 0xFF);
                }

                for (int j = len; j >= 0; --j, value /= 61)
                    tmp[j] = toBase62[value % 61];

                ret.Append(tmp, 0, len + 1);
            }

            return ret.ToString();
        }

        public static byte[] decode(string txt)
        {
            int len = txt.Length;
            fill();
            byte[] dst = new byte[len / 4 * 3 + ((len % 4 > 0) ? len % 4 - 1 : 0)];
            byte[] tmp = new byte[3];

            int value = 0;
            int val = 0;
            char ch = (char)0;
            int count = 0;
            int bi = 0;
            int isFX = 0;
            for (int i = 0; i < len; ++i)
            {
                ch = txt[i];
                if (ch >= 0x80)
                    throw new ArgumentException("Illegal base62 character " + ch);

                val = fromBase62[ch];
                if (val < 0)
                {
                    if (val == -2 && (count % 4) == 0 && isFX == 0)
                    {
                        isFX = 1;
                        continue;
                    }

                    throw new ArgumentException("Illegal base62 character " + ch);
                }
                ++count;

                value = value * 61 + val;
                if (count % 4 == 0)
                {
                    if (isFX != 0)
                    {
                        isFX = value >> 20;
                        for (int j = 2, mask = 1; j >= 0; --j, mask <<= 1)
                        {
                            tmp[j] = (byte)(value & 0x0F); value >>= 4;
                            if ((isFX & mask) == mask)  { tmp[j] |= 0xF0; }
                            else                        { tmp[j] |= (byte)((value & 0x0F) << 4); value >>= 4; }
                        }
                        isFX = 0;
                    }
                    else
                    {
                        for (int j = 2; j >= 0; --j, value /= 0xF0)
                            tmp[j] = (byte)(value % 0xF0);
                    }

                    value = 0;
                    Array.Copy(tmp, 0, dst, bi, 3);
                    bi += 3;
                }
            }

            len = count % 4;
            if (len > 0)
            {
                len -= 1;
                if (isFX != 0)
                {
                    isFX = value >> ((len >= 2) ? 12 : 4);
                    for (int j = len - 1, mask = 1; j >= 0; --j, mask <<= 1)
                    {
                        tmp[j] = (byte)(value & 0x0F); value >>= 4;
                        if ((isFX & mask) == mask)  { tmp[j] |= 0xF0; }
                        else                        { tmp[j] |= (byte)((value & 0x0F) << 4); value >>= 4; }
                    }
                }
                else
                {
                    for (int j = len - 1; j >= 0; --j, value /= 0xF0)
                        tmp[j] = (byte)(value % 0xF0);
                }

                Array.Copy(tmp, 0, dst, bi, len);
                bi += len;
            }

            if (bi < dst.Length)
            {
                byte[] ret = new byte[bi];
                Array.Copy(dst, 0, ret, 0, bi);
                return ret;
            }

            return dst;
        }

        public static string bin2hexa(byte[] bin)
        {
            StringBuilder ret = new StringBuilder();
            for (int i = 0, len = bin.Length; i < len; ++i)
            {
                ret.AppendFormat("{0:X02} ", bin[i]);
                if (i % 16 == 7) ret.Append(' ');
                else if (i % 16 == 15) ret.Append('\n');
            }

            if (bin.Length % 16 != 0) ret.Append('\n');

            return ret.ToString();
        }
    }
}
