#include "an61.h"
#include "zstring.h"
#include <vector>
#include <string.h>

static char toBase62[62] = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
};

std::string AN61::encode(const std::wstring& text)
{
	std::string utf8 = wstring2utf8(text) ;
	std::string ret ;
	long value = 0 ;
	int val = 0 ;
	int len = utf8.length() ;
	char tmp[4] ;
	for(int i = 0; i < len; ++i) {
		val = (utf8[i] & 0xFF) ;
		if(val >= 0xF0)	{
			AN61::invalid_character_exception ice ;
			ice.msg = "invalid UCS2 character" ;
			throw ice ;
		}

		value = value * 0xF0 + val;
		if(i % 3 == 2) {
			for(int j = 3; j >= 0; --j, value /= 61)
				tmp[j] = toBase62[(int)(value % 61)];
			value = 0 ;
			ret.append(tmp, tmp+4) ;
		}
	}

	len = utf8.length() % 3 ;
	if(len > 0) {
		for(int j = len; j >= 0; --j, value /= 61)
			tmp[j] = toBase62[(int)(value % 61)];

		ret.append(tmp, tmp + len+1) ;
	}

	return ret ;
}

std::wstring AN61::decode(const std::string& text)
{
	int len = text.length() ;
	if(len % 4 == 1)	return L"" ;

	std::vector<unsigned char> dst ;
	dst.resize(len / 4 * 3 + ((len % 4 > 0) ? len%4 - 1 : 0), 0x00) ;
	std::vector<unsigned char> tmp ;
	tmp.resize(3, 0x00) ;

	std::vector<char> fromBase62 ;
	fromBase62.resize(128, -1) ;
	for (int i = 0; i < 62; i++)
		fromBase62[toBase62[i]] = i;
	fromBase62['z'] = -2;

	int value = 0 ;
	int val = 0 ;
	char ch = 0 ;

	int bi = 0 ;
	for(int i = 0; i < len; ++i) {
		ch = text[i] ;
		if(ch >= 0x80)	return L"" ;
			
		val = fromBase62[ch] ;
		if(val < 0)	return L"" ;
			
		value = value * 61 + val;
		if(i % 4 == 3) {
			for(int j = 2; j >= 0; --j, value /= 0xF0)
				tmp[j] = value % 0xF0 ;

			memcpy(&dst[bi], &tmp[0], 3) ;
			bi += 3 ;
			value = 0 ;
		}
	}

	len = len % 4 ;
	if(len > 0) {
		len -= 1 ;
		for(int j = len-1; j >= 0; --j, value /= 0xF0)
			tmp[j] = value % 0xF0 ;

		memcpy(&dst[bi], &tmp[0], len) ;
		bi += len ;
	}

	dst.resize(bi) ;
	std::string utf8(dst.cbegin(), dst.cend()) ;
	return utf8_to_wstring(utf8) ;
}
