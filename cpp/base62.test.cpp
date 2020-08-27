// charset : ASCII
#include "base62.h"
#include <stdio.h>

std::string bin2hexa(const std::vector<unsigned char>& bin)
{
	std::string ret ;
	char tmp[4] ;
	for (size_t i = 0, len = bin.size(); i < len; ++i) {
		sprintf(tmp, "%02X ", bin[i] & 0xFF) ;
		ret.append(tmp) ;
		if(i % 16 == 7)			ret.push_back(' ') ;
		else if(i % 16 == 15)	ret.push_back('\n') ;
	}
	if(bin.size() % 16 != 0)	ret.push_back('\n') ;
	return ret ;
}

int main(int argc, char *argv[])
{
	std::vector<unsigned char> bin ;
	bin.resize(256) ;
	for(size_t i = 0, len = bin.size(); i < len; ++i)
		bin[i] = (unsigned char)i ;

	printf("----bin[%zu]----\n", bin.size()) ;
	printf("%s\n", bin2hexa(bin).c_str()) ;

	std::string txt = Base62::encode(bin) ;
	printf("txt[%zu]:%s\n", txt.size(), txt.c_str()) ;

	std::vector<unsigned char> out = Base62::decode(txt) ;
	printf("----out[%zu]----\n", out.size()) ;
	printf("%s\n", bin2hexa(out).c_str()) ;

	return 0 ;
}
