// charset : EUC-KR(on Windows)
#include "an61.h"
#include "zstring.h"
#include "base62.h"
#include <stdio.h>

int main(int argc, char *argv[])
{
	std::string locale(setlocale(LC_ALL, "")) ;
	printf("locale : [%s]\n", locale.c_str()) ;
	printf("sizeof(wchar_t) : %zd\n", sizeof(wchar_t)) ;

	{
		printf("---wstring to wstring---\n") ;
		std::wstring src0 = L"http://test.com:8080/an62.do?name=°¡³ª´Ù ¤¡¤¤¡Ø\nÊ¦" ;
		printf("src0[%zd]:%s\n", src0.length(), wstring2system(src0).c_str()) ;
		std::string tmp0 = AN61::encode(src0) ;
		printf("tmp0:%s\n", tmp0.c_str()) ;
		std::wstring out0 = AN61::decode(tmp0) ;
		printf("out0:%s\n", wstring2system(out0).c_str()) ;
		printf("src0.compare(out0) : %d\n", src0.compare(out0)) ;
	}

	{
		if(isSystemUTF8())  printf("---UTF8 to UTF8---\n") ;
		else                printf("---EUC-KR to EUC-KR---\n") ;
		std::string src0 = "http://test.com:8080/an62.do?name=°¡³ª´Ù ¤¡¤¤¡Ø\nÊ¦" ;
		printf("src0[%zd]:%s\n", src0.length(), src0.c_str()) ;
		std::string tmp0 = AN61::encode(system2wstring(src0)) ;
		printf("tmp0:%s\n", tmp0.c_str()) ;
		std::string out0 = wstring2system(AN61::decode(tmp0)) ;
		printf("out0:%s\n", out0.c_str()) ;
		printf("src0.compare(out0) : %d\n", src0.compare(out0)) ;
	}
    
	{
		printf("---wstring to wstring---\n") ;
		// [ ÄÚ³¢¸® = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
		std::wstring src1 = L"http://test.com:8080/an62.do?name=°¡³ª´Ù ¤¡¤¤¡Ø\nÊ¦" ;
		if(sizeof(wchar_t) == 2) {	// Windows
			src1.push_back((wchar_t)0xD83D) ;
			src1.push_back((wchar_t)0xDC18) ;
		}
		else {	// Linux
			src1.push_back((wchar_t)0x01F418) ;
		}
		printf("src1[%zd]:%s\n", src1.length(), wstring2system(src1).c_str()) ;
		try {
			std::string tmp1 = AN61::encode(src1) ;
			printf("tmp1:%s\n", tmp1.c_str()) ;
			std::wstring out1 = AN61::decode(tmp1) ;
			printf("out1:%s\n", wstring2system(out1).c_str()) ;
		}
		catch(const std::exception& e) {
			fprintf(stderr, "Error : %s\n", e.what()) ;
			
			std::string utf8 = wstring2utf8(src1) ;
			std::vector<unsigned char> utf8_bin(utf8.cbegin(), utf8.cend()) ;
			std::string tmp2 = Base62::encode(utf8_bin) ;
			printf("tmp2[%zu]:%s\n", tmp2.size(), tmp2.c_str()) ;

			std::vector<unsigned char> out2_bin = Base62::decode(tmp2) ;
			std::string utf8_str(out2_bin.cbegin(), out2_bin.cend()) ;
			std::wstring out2 = utf8_to_wstring(utf8_str) ;
			printf("out2[%zu]:%s\n", out2.size(), wstring2system(out2).c_str()) ;

			printf("src1.compare(out2) : %d\n", src1.compare(out2)) ;
		}
	}

	{
		printf("---utf8 to utf8---\n") ;
		// [ ÄÚ³¢¸® = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
		std::string utf8 = system2utf8("http://test.com:8080/an62.do?name=°¡³ª´Ù ¤¡¤¤¡Ø\nÊ¦") ;
		utf8.push_back((char)0xF0) ;
		utf8.push_back((char)0x9F) ;
		utf8.push_back((char)0x90) ;
		utf8.push_back((char)0x98) ;
		printf("utf8[%zd]:%s\n", utf8.length(), utf8_to_system(utf8).c_str()) ;
		try {
			std::string tmp1 = AN61::encode(utf8_to_wstring(utf8)) ;	
			printf("tmp1:%s\n", tmp1.c_str()) ;
			std::string out8 = wstring2utf8(AN61::decode(tmp1)) ;
			printf("out8[%zd]:%s\n", out8.length(), utf8_to_system(out8).c_str()) ;
		}
		catch(const std::exception& e) {
			fprintf(stderr, "Error : %s\n", e.what()) ;

			std::vector<unsigned char> utf8_bin(utf8.cbegin(), utf8.cend()) ;
			std::string tmp8 = Base62::encode(utf8_bin) ;
			printf("tmp8[%zu]:%s\n", tmp8.size(), tmp8.c_str()) ;

			std::vector<unsigned char> out8_bin = Base62::decode(tmp8) ;
			std::string out8(out8_bin.cbegin(), out8_bin.cend()) ;
			printf("out8[%zu]:%s\n", utf8.size(), utf8_to_system(utf8).c_str()) ;

			printf("utf8.compare(out8) : %d\n", utf8.compare(out8)) ;
		}
	}

	return 0 ;
}
