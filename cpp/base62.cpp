#include "base62.h"
#include <vector>
#include <string.h>

static char toBase62[62] = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
};

std::string	Base62::encode(const std::vector<unsigned char>& bin)
{
	std::string ret ;
	int value = 0 ;
	int len = bin.size() / 3 * 3;
	char tmp[4] ;
	int FX_bit = 0 ;
	int i = 0 ;

	for(i = 0; i < len; i += 3) {
		FX_bit = 0 ;
		value = 0 ;
		
		for(int j = 0; j < 3; ++j) {
			FX_bit <<= 1 ;
			value <<= 4 ;
			if((bin[i+j] & 0xF0) == 0xF0) {
				FX_bit |= 0x01 ;
			}
			else {
				value |= (bin[i+j] >> 4) & 0x0F ;
				value <<= 4 ;
			}
			value |= bin[i+j] & 0x0F ;
		}

		if (FX_bit) {
			ret.append(1, 'z') ;
			value |= FX_bit << 20 ;
		}
		else {
			value = 0 ;
			for(int j = 0; j < 3; ++j)
				value = value * 0xF0 + bin[i+j];
		}

		for(int j = 3; j >= 0; --j, value /= 61)
			tmp[j] = toBase62[value % 61];

		ret.append(tmp, tmp+4) ;
	}

	len = bin.size() % 3 ;
	if(len > 0) {
		FX_bit = 0 ;
		value = 0 ;
		for(int j = 0; j < len; ++j) {
			FX_bit <<= 1 ;
			value <<= 4 ;
			if((bin[i+j] & 0xF0) == 0xF0) {
				FX_bit |= 0x01 ;
			}
			else {
				value |= (bin[i+j] >> 4) & 0x0F ;
				value <<= 4 ;
			}
			value |= bin[i+j] & 0x0F ;
		}

		if (FX_bit) {
			ret.append(1, 'z') ;
			value |= FX_bit << ((len == 1) ? 4 : 12) ;
		}
		else {
			value = 0 ;
			for(int j = 0; j < len; ++j)
				value = value * 0xF0 + bin[i+j];
		}

		for(int j = len; j >= 0; --j, value /= 61)
			tmp[j] = toBase62[value % 61];

		ret.append(tmp, tmp+len+1) ;
	}

	return ret ;
}

std::vector<unsigned char> Base62::decode(const std::string& txt)
{
	int len = txt.length() ;
	
	std::vector<unsigned char> dst ;
	dst.resize(len / 4 * 3 + ((len % 4 > 0) ? len%4 - 1 : 0), 0x00) ;

	std::vector<unsigned char> tmp ;
	tmp.resize(3, 0x00) ;

	std::vector<char> fromBase62 ;
	fromBase62.resize(128, -1) ;
	for (int i = 0; i < 62; i++)
		fromBase62[toBase62[i]] = i;
	fromBase62['z'] = -2;

	long value = 0 ;
	int val = 0 ;
	char ch = 0 ;
	int count = 0 ;	
	int bi = 0 ;
	int isFX = 0 ;
	for(int i = 0; i < len; ++i) {
		ch = txt[i] ;
		if(ch >= 0x80)	{	dst.clear() ;	return dst ;	}
			
		val = fromBase62[ch] ;
		if(val < 0) {
			if(val == -2 && (count % 4) == 0 && isFX == 0) {
				isFX = 1 ;
				continue ;
			}
			dst.clear() ;
			return dst ;
		}
		++count ;

		value = value * 61 + val;
		if(count % 4 == 0) {
			if (isFX) {
				isFX = value >> 20 ;
				for (int j = 2, mask=1; j >= 0; --j, mask <<= 1) {
					tmp[j] = value & 0x0F ;	value >>= 4 ;
					if (isFX & mask)	{	tmp[j] |= 0xF0 ;	}
					else				{	tmp[j] |= (value & 0x0F) << 4 ;	value >>= 4 ;	}
				}
				isFX = 0 ;
			}
			else {
				for(int j = 2; j >= 0; --j, value /= 0xF0)
					tmp[j] = value % 0xF0 ;
			}
			value = 0 ;

			memcpy(&dst[bi], &tmp[0], 3) ;
			bi += 3 ;
		}
	}

	len = count % 4 ;
	if(len > 0) {
		len -= 1 ;
		if (isFX) {
			isFX = value >> ((len >= 2) ? 12 : 4) ;
			for (int j = len-1, mask=1; j >= 0; --j, mask <<= 1) {
				tmp[j] = value & 0x0F ;	value >>= 4 ;
				if (isFX & mask)	{	tmp[j] |= 0xF0 ;	}
				else				{	tmp[j] |= (value & 0x0F) << 4 ;	value >>= 4 ;	}
			}
		}
		else {
			for(int j = len-1; j >= 0; --j, value /= 0xF0)
				tmp[j] = value % 0xF0 ;
		}

		memcpy(&dst[bi], &tmp[0], len) ;
		bi += len ;
	}
	
	dst.resize(bi) ;
	return dst ;
}
