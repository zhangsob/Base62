#ifndef _AN61_H_
#define _AN61_H_

#include <string>
#include <vector>
#include <exception>

struct AN61 {
struct invalid_character_exception : std::exception {
    const char* what() const noexcept { return msg.c_str(); }
    std::string msg ;
};

static std::string	encode(const std::wstring& text) ;
static std::wstring	decode(const std::string& text) ;
};
#endif
