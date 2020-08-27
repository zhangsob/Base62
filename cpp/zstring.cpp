#include "zstring.h"

#include <cstdarg>
#include <cstring>
#include <locale>
//#include <codecvt>
#include <vector>

#if defined(_MSC_VER)
#	include <windows.h>
#else
#	include <errno.h>
#	include <iconv.h>
#endif

bool isSystemUTF8()
{
	std::string locale(setlocale(LC_ALL, "")) ;
	
	if(strstr(locale.c_str(), "UTF-8"))	return true ;
	if(strstr(locale.c_str(), "UTF8"))	return true ;
	if(strstr(locale.c_str(), "utf-8"))	return true ;
	if(strstr(locale.c_str(), "utf8"))	return true ;
	if(strstr(locale.c_str(), "65001"))	return true ;
	
	return false ;
}

std::string fstr(const char *format, ...)
{
	std::string ret ;
	va_list args ;

	va_start(args, format) ;
	size_t size = std::vsnprintf(nullptr, 0, format, args) + 1;
	va_end(args) ;

	ret.resize(size, 0x00) ;

	va_start(args, format) ;
	std::vsnprintf(&ret[0], ret.size(), format, args);
	va_end(args) ;

	ret.resize(strlen(ret.c_str())) ;
	return ret ;
}

int toInteger(const std::string& digit, int default_value)
{
	int ret = 0 ;
	if (digit.empty())	return default_value ;

	for(char d : digit) {
		if(!isdigit(d))	return default_value ;
		ret = ret * 10 + (d - '0') ;
	}
	return ret ;
}

#if defined(_ICONV_H) || defined(_LIBICONV_H)
static bool isLitteEndian()
{
	short int si = 0x1234 ;
	return (memcmp(&si, "\x12\x34", 2) != 0) ;
}
#endif

std::wstring string2wstring(const std::string& ansi)
{
	std::wstring ret ;
#if defined(_MSC_VER)
	ret.resize(ansi.length()+1) ;
	int len = ::MultiByteToWideChar(CP_ACP, 0, (LPCSTR)ansi.data(), ansi.length(), &ret[0], ret.size()) ;
	ret.resize(len >= 0 ? len : 0) ;
#elif defined(_ICONV_H) || defined(_LIBICONV_H)
	bool isLE = isLitteEndian() ;
	iconv_t ic = iconv_open(isLE ? "UCS-2LE" : "UCS-2BE" /*tocode*/, "CP949" /*fromcode*/ );
	if (ic == (iconv_t)-1)	return ret ;

	size_t in_size = ansi.length() ;
	size_t out_size = in_size * 2 + 1;
	std::vector<unsigned char> tmp ;
	tmp.resize(out_size, 0x00) ;
	
	char* in_ptr = (char *)&ansi[0];
	char* out_ptr = (char *)&tmp[0];
	size_t out_buf_left = out_size;

	size_t result = iconv(ic, &in_ptr, &in_size, &out_ptr, &out_buf_left);

	iconv_close(ic);
	if (result == (size_t)-1)	return ret ;
	int ret_len = out_size - out_buf_left ;

	ret_len /= 2 ;
	ret.resize(ret_len) ;
	unsigned short *us = (unsigned short *)&tmp[0] ;
	if(isLE) {
		for(int i = 0;i < ret_len; ++i) {
			ret[i] = us[i] ;
		}
	}
	else {
		for(int i = 0;i < ret_len; ++i)
			ret[i] = us[i] ;
	}
#endif
	return ret ;
}

std::string wstring2string(const std::wstring& unicode)
{
	std::string ret ;
#if defined(_MSC_VER)
	ret.resize(unicode.length()*2+1) ;
	int len = ::WideCharToMultiByte(CP_ACP, 0, (LPWSTR)unicode.data(), unicode.length(), &ret[0], ret.size(), NULL, NULL) ;
	ret.resize(len >= 0 ? len : 0) ;
#elif defined(_ICONV_H) || defined(_LIBICONV_H)
	bool isLE = isLitteEndian() ;
	size_t wc_size = sizeof(wchar_t) ;

	std::string from_code = "UCS-" ;
	from_code.push_back((char)('0' + wc_size)) ;
	from_code += isLE ? "LE" : "BE" ;
#if !defined(_CODECVT_)
	if(wc_size == 2) {
		from_code = "UTF-16" ;
		from_code += isLE ? "LE" : "BE" ;
	}
#endif

	iconv_t ic = iconv_open("CP949//IGNORE" /*tocode*/, from_code.c_str() /*fromcode*/ );
	if (ic == (iconv_t)-1)	return ret ;
	
	size_t in_size = unicode.length() * wc_size ;
	size_t out_size = unicode.length() * 2 + 1;

	ret.resize(out_size, 0x00) ;
	
	char* in_ptr = (char *)unicode.data();
	char* out_ptr = (char *)ret.data();
	size_t out_buf_left = out_size;

	size_t result = iconv(ic, &in_ptr, &in_size, &out_ptr, &out_buf_left);

	iconv_close(ic);
	if (result == (size_t)-1) {
#if 0
  		char errmsg[10] = "";
  		if (errno == E2BIG) sprintf(errmsg, "E2BIG");
  		if (errno == EILSEQ) sprintf(errmsg, "EILSEQ");
  		if (errno == EINVAL) sprintf(errmsg, "EINVAL");
  		fprintf(stderr, "Fail to iconv() errorno : %s(%d)\n", errmsg, errno);
#endif
		ret.clear() ;
	}
	else
		ret.resize(out_size - out_buf_left) ;
#endif
	return ret ;
}

std::wstring utf8_to_wstring(const std::string& utf8)
{
#if defined(_CODECVT_)
	std::wstring_convert<std::codecvt_utf8<wchar_t>> wconv;
	return wconv.from_bytes(utf8);
#else
	std::wstring ret ;
	int count = 0 ;
	unsigned int value = 0 ;

	for(const auto& val : utf8) {
		if(count == 0) {
			if((val & 0xF8) == 0xF0) {
				count = 3 ;
				value = val & 0x07 ;
			}
			else if((val & 0xF0) == 0xE0) {
				count = 2 ;
				value = val & 0x0F ;
			}
			else if((val & 0xE0) == 0xC0) {
				count = 1 ;
				value = val & 0x1F ;
			}
			else {
				ret.push_back(val) ;
				value = 0 ;
			}
		}
		else {
			//if((val & 0xC0) != 0x80) throw std::excep("Illegal UTF8") ;

			value <<= 6 ;
			value |= val & 0x3F ;

			if(--count == 0) {
				if(value > 0xFFFF && sizeof(wchar_t) == 2) {	// UTF-16
					ret.push_back(0xD800 | (((value >> 16) - 1) << 6) | ((value >> 10) & 0x003F)) ;
					ret.push_back(0xDC00 | (value & 0x03FF)) ;
				}
				else {	// UTF-32
					ret.push_back(value) ;
				}
				value = 0 ;
			}
		}
	}

	return ret ;
#endif
}

std::string wstring2utf8(const std::wstring& wstr)
{
#if defined(_CODECVT_)
    std::wstring_convert<std::codecvt_utf8<wchar_t>> wconv;
	return wconv.to_bytes(wstr);
#else
	std::string ret ;
	unsigned int value = 0 ;
    for(const auto& wc : wstr) {
		if((wc & 0xF800) == 0xD800) {
			if(wc & 0x0400) {	// Low-Surrogate : 0xDC00 ~ 0xDFFF
				value = (value << 10) | (wc & 0x03FF) ;
			}
			else {				// High-Surrogate : 0xD800 ~ 0xDBFF 
				value = (wc & 0x03FF) + 0x40 ;
				continue ;
			}
		}
		else {
			value = wc ;
		}

		if(value < 0x0080) {
			ret.push_back(value) ;
		}
		else if(value < 0x0800) {
			ret.push_back(0xC0 | (value >> 6)) ;
			ret.push_back(0x80 | (value & 0x3F)) ;
		}
		else if(value < 0x010000) {
			ret.push_back(0xE0 | (value >> 12)) ;
			ret.push_back(0x80 | ((value >> 6) & 0x3F)) ;
			ret.push_back(0x80 | (value & 0x3F)) ;
		}
		else {
			ret.push_back(0xF0 | (value >> 18)) ;
			ret.push_back(0x80 | ((value >> 12) & 0x3F)) ;
			ret.push_back(0x80 | ((value >> 6) & 0x3F)) ;
			ret.push_back(0x80 | (value & 0x3F)) ;
		}
	}

	return ret ;
#endif
}

std::string utf8_to_string(const std::string& utf8)
{
	std::wstring wstr = utf8_to_wstring(utf8) ;
	return wstring2string(wstr) ;
}

std::string string2utf8(const std::string& ansi)
{
	std::wstring wstr = string2wstring(ansi) ;
	return wstring2utf8(wstr) ;
}

std::wstring system2wstring(const std::string& txt)
{
	return isSystemUTF8() ? utf8_to_wstring(txt) : string2wstring(txt) ;
}

std::string wstring2system(const std::wstring& unicode)
{
	return isSystemUTF8() ? wstring2utf8(unicode) : wstring2string(unicode) ;
}

std::string utf8_to_system(const std::string& txt)
{
	return isSystemUTF8() ? txt : utf8_to_string(txt) ;
}

std::string system2utf8(const std::string& txt)
{
	return isSystemUTF8() ? txt : string2utf8(txt) ;
}
