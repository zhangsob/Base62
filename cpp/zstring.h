#ifndef _ZSTRING_H_
#define _ZSTRING_H_

#include <string>
#include <vector>

bool isSystemUTF8() ;
std::string fstr(const char *format, ...) ;
int toInteger(const std::string& digit, int default_value) ;

std::wstring string2wstring(const std::string& ansi) ;
std::string wstring2string(const std::wstring& unicode) ;

std::wstring utf8_to_wstring(const std::string& utf8) ;
std::string wstring2utf8(const std::wstring& unicode) ;

std::string utf8_to_string(const std::string& utf8) ;
std::string string2utf8(const std::string& ansi) ;

std::wstring system2wstring(const std::string& txt) ;
std::string wstring2system(const std::wstring& unicode) ;

std::string utf8_to_system(const std::string& txt) ;
std::string system2utf8(const std::string& txt) ;

#endif //_ZSTRING_H_
