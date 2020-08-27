#ifndef _BASE62_H_
#define _BASE62_H_

#include <string>
#include <vector>

struct Base62 {
static std::string encode(const std::vector<unsigned char>& bin) ;
static std::vector<unsigned char> decode(const std::string& txt) ;
};
#endif
