# BASE62 (AN61 : AlphaNumeric61)
BASE64 Encode에는 +/= 기본형 또는 -_= URL형이 있다.  
여기서, 항상 특수문자가 문제가 되어, 0&#126;9, A&#126;Z, a&#126;z만으로 이루어진 Base62 (및 AN61 : AlphaNumeric61)을 만들어 보았다.

## 원리
- AN61  

|        Unicode값       |                 UTF-8                   |       Byte단위 값의 범위        |  비고 |
|------------------------|:----------------------------------------|:-------------------------------|-------|
| 0x000000&#126;0x00007F | 0xxx xxxx                               | 0x00&#126;0x7F                 | ASCII |
| 0x000080&#126;0x0007FF | 110x xxxx 10xx xxxx                     | 0xC0&#126;0xDF, 0x80&#126;0xBF | 유럽   |
| 0x000800&#126;0x00FFFF | 1110 xxxx 10xx xxxx 10xx xxxx           | 0xE0&#126;0xEF, 0x80&#126;0xBF | 한글등 |
| 0x010000&#126;0x10FFFF | 1111 0zzz 10zz xxxx 10xx xxxx 10xx xxxx | 0xF0&#126;0xF7, 0x80&#126;0xBF |       |

여기서, 0x0000 ~ 0xFFFF까지 UCS2에 거의 세계의 주요 문자가 속한다.   
(실제로, java나 C#에서 String.length(), String.Length은 문자의 수가 아닌 UTF-16의 길이이다.)  

그래서, 0x00&#126;0x7F, 0x80&#126;0xBF, 0xC0&#126;0xDF, 0xE0&#126;0xEF 즉, 0x00&#126;0xEF(240가지)만 주로 사용된다.   
240<sup>3</sup> < 61<sup>4</sup> ( 13,824,000 < 13,845,841 ) 이다. (즉, 240가지 3덩어리를 61가지 4덩어리로 표현가능하다.)  

BASE64 Encode의 원리도  
256<sup>3</sup> = 64<sup>4</sup> 즉, 2<sup>(8&#42;3)</sup> = 2<sup>(6&#42;4)</sup> 로 3Byte을 6bit씩 4덩어리로 표현한 것이다.  

- Base62

여기서, 62가지중 'z'를 escape(0xFX영역)하여 Binary도 지원한다.  
Base62 [ Binary To String Encoding / Decoding(String To Binary) ]에서는 아래와 같이 한다.

|             3 Byte            |             Value             |
|-------------------------------|-------------------------------|
| xxxx xxxx yyyy yyyy 1111 zzzz |  001 xxxx xxxx yyyy yyyy zzzz |
| xxxx xxxx 1111 yyyy zzzz zzzz |  010 xxxx xxxx yyyy zzzz zzzz |
| xxxx xxxx 1111 yyyy 1111 zzzz |  011 0000 xxxx xxxx yyyy zzzz |
| 1111 xxxx yyyy yyyy zzzz zzzz |  100 xxxx yyyy yyyy zzzz zzzz |
| 1111 xxxx yyyy yyyy 1111 zzzz |  101 0000 xxxx yyyy yyyy zzzz |
| 1111 xxxx 1111 yyyy zzzz zzzz |  110 0000 xxxx yyyy zzzz zzzz |
| 1111 xxxx 1111 yyyy 1111 zzzz |  111 0000 0000 xxxx yyyy zzzz |  

3Byte중 0xF0 ~ 0xFF ( 0xFX )에 해당 Byte을 위와 같이 Value를 전환하여 보면, 23bit로 표현이 가능하다.  
상위 3bit는 0xFX의 위치를 표시하며, 하위 20bit가 나머지 값이다.

2<sup>23</sup> < 240<sup>3</sup> < 61<sup>4</sup> ( 8,388,608 < 13,824,000 < 13,845,841 ) 이므로 표현이 가능하다.

|       2 Byte        |        Value        |
|---------------------|---------------------|
| xxxx xxxx 1111 yyyy |   01 xxxx xxxx yyyy |
| 1111 xxxx yyyy yyyy |   10 xxxx yyyy yyyy |
| 1111 xxxx 1111 yyyy |   11 0000 xxxx yyyy |  

2<sup>14</sup> < 240<sup>2</sup> < 61<sup>3</sup> ( 16,384 < 57,600 < 226,981 ) 이므로 표현이 가능하다.

|  1 Byte   |    Value  |
|-----------|-----------|
| 1111 xxxx |    1 xxxx |  

2<sup>5</sup> < 240<sup>1</sup> < 61<sup>2</sup> ( 32 < 240 < 3,721 ) 이므로 표현이 가능하다.

## Encoding
Base62 : Binary to AlphaNumeric Only String Encoding  
AN61 : UCS2 String to AlphaNumeric Only String Encoding  
AN62 : All Unicode String to AlphaNumeric Only String Encoding - [https://github.com/zhangsob/AN62](https://github.com/zhangsob/AN62) 참조

## 장단점
단점 : BASE64는 bit연산으로 구현하고, Base62(AN61)는 산술연산으로 다소 속도는 느림  

장점 : 특수문자(기호)가 없어 어떠한 환경에서 값으로 사용할 수 있음.  

## 지원언어
아래 언어로 소스코드를 올립니다. 
- java : [BASE62 예](#base62_java), [AN61 예](#an61_java)
- javascript : [BASE62 예](#base62_javascript), [AN61 예](#an61_javascript)
- python : [BASE62 예](#base62_python), [AN61 예](#an61_python)
- c# : [BASE62 예](#base62_csharp), [AN61 예](#an61_csharp)
- cpp : [BASE62 예 (Windows)](#base62_cpp), [AN61 예 (Windows)](#an61_cpp_windows), [AN61 예 (Linux)](#an61_cpp_linux)
- pascal : [BASE62 예](#base62_pascal), [AN61 예 (Delphi)](#an61_pascal_delphi), [AN61 예 (Free Pascal)](#an61_pascal_free_pascal)
- php : [BASE62 예](#base62_php), [AN61 예](#an61_php)
- kotlin : [BASE62 예](#base62_kotlin), [AN61 예](#an61_kotlin)

<a name="base62_java"></a>
## Java BASE62 예
```java
public static void main(String[] args) {
    try {
        byte[] bin = new byte[256] ;
        for(int i = 0; i < bin.length; ++i)
            bin[i] = (byte)i ;
            
        System.out.println("----bin["+bin.length+"]----") ;
        System.out.println(Base62.bin2hexa(bin)) ;

        String txt = Base62.encode(bin) ;
        System.out.println("txt["+txt.length()+"]:" + txt) ;

        byte[] out = Base62.decode(txt) ;
        System.out.println("----out["+out.length+"]----") ;
        System.out.println(Base62.bin2hexa(out)) ;
    } catch(Exception e) {
        e.printStackTrace();
    }
}
```
-----------------------------------------------------------------------------------
```
----bin[256]----
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F 
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F 
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F 
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F 
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F 
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F 
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F 
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F 
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F 
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F 
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF 
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF 
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF 
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF 
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF 
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF 

txt[348]:003x0kgb1WKF2Hws33aW3oEA4Zqn5LUR67856rki7dOM8P209Aed9vIHAguuBSYYCECCCyopDkSTEW67FHikG3MOGo02HZcfILGJJ6swJrWaKdAELOmrMAQVMv49NggmOSKQPDx4PyahQkELRVqySHUcT38GTnktUZOXVL2BW6eoWrISXcv6YOYjZACNZup1agSebS6IcDivcyMZdk0DeVcqfHGUg2t8gnWlhZAPiKn3j6Qgjr4KkcgxlOKbm9xFmuasngEWoRrApDUnpy8Rqjl5rVOisH2Mt2f0tnIduYvHvKYuw6CYwqpCxcSpyO6TzWKagzWKo7zWL2XzWLFxzWLTOz0V
----out[256]----
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F 
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F 
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F 
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F 
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F 
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F 
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F 
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F 
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F 
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F 
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF 
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF 
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF 
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF 
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF 
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF 
```

<a name="an61_java"></a>
## Java AN61 예
```java
public static void main(String[] args) {
    try {
        String src0 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可" ;
        System.out.println("src0["+src0.length()+"]:" + src0) ;
        String an61__tmp0 = AN61.encode(src0) ;
        System.out.println("an61__tmp0["+an61__tmp0.length()+"]:" + an61__tmp0) ;
        String an61__out0 = AN61.decode(an61__tmp0) ;
        System.out.println("an61__out0["+an61__out0.length()+"]:" + an61__out0) ;
        
        System.out.println("src0.equals(an61__out0) : " + src0.equals(an61__out0)) ;
        
        String base64_tmp = java.util.Base64.getEncoder().encodeToString(src0.getBytes("utf8")) ;
        System.out.println("base64_tmp["+base64_tmp.length()+"]:" + base64_tmp) ;
        String base64_out = new String(java.util.Base64.getDecoder().decode(base64_tmp), "utf8") ;
        System.out.println("base64_out["+base64_out.length()+"]:" + base64_out) ;

        // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
        String src1 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可🐘" ;	// UnsupportedEncodingException이 발생하는 경우
        System.out.println("src1["+src1.length()+"]:" + src1) ;		// String.length()은 문자갯수가 아니라, UTF16의 길이다. 
        try {
            String tmp1 = AN61.encode(src1) ;
            System.out.println("tmp1:" + tmp1) ;
            String out1 = AN61.decode(tmp1) ;
            System.out.println("out1:" + out1) ;
        } catch(UnsupportedEncodingException uee) {
            System.err.println(uee) ;

            String tmp2 = Base62.encode(src1.getBytes("utf8")) ;
            System.out.println("tmp2["+tmp2.length()+"]:" + tmp2) ;
            String out2 = new String(Base62.decode(tmp2), "utf8") ;
            System.out.println("out2["+out2.length()+"]:" + out2) ;
            
            System.out.println("src1.equals(out2) : " + src1.equals(out2)) ;
        }
    } catch(Exception e) {
        e.printStackTrace();
    }
}
```
-----------------------------------------------------------------------------------
```
src0[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
an61__tmp0[76]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
an61__out0[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
src0.equals(an61__out0) : true
base64_tmp[76]:aHR0cDovL3Rlc3QuY29tOjgwODAvYW42MS5kbz9uYW1lPeqwgOuCmOuLpCDjhLHjhLTigLsK5Y+v
base64_out[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
src1[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可🐘
java.io.UnsupportedEncodingException: invalid UCS2 character index 43 🐘
tmp2[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
out2[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可🐘
src1.equals(out2) : true
```

<a name="base62_javascript"></a>
## JavaScript BASE62 예
```javascript
try {
    var bin = [], i = 0 ;
    for(i = 0;i < 256; ++i)
        bin[i] = i ;

    print('----bin['+bin.length+']----') ;
    print(Base62.bin2hexa(bin)) ;

    var txt = Base62.encode(bin) ;
    print('txt['+txt.length+']:' + txt) ;

    var out = Base62.decode(txt) ;
    print('----out['+out.length+']----') ;
    print(Base62.bin2hexa(out)) ;
} catch(e) {
    print(e) ;
}
```
-----------------------------------------------------------------------------------
```
----bin[256]----
00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57 58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77 78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87 88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97 98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7 A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7 B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7 C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7 D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7 E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7 F8 F9 FA FB FC FD FE FF

txt[348]:003x0kgb1WKF2Hws33aW3oEA4Zqn5LUR67856rki7dOM8P209Aed9vIHAguuBSYYCECCCyopDkSTEW67FHikG3MOGo02HZcfILGJJ6swJrWaKdAELOmrMAQVMv49NggmOSKQPDx4PyahQkELRVqySHUcT38GTnktUZOXVL2BW6eoWrISXcv6YOYjZACNZup1agSebS6IcDivcyMZdk0DeVcqfHGUg2t8gnWlhZAPiKn3j6Qgjr4KkcgxlOKbm9xFmuasngEWoRrApDUnpy8Rqjl5rVOisH2Mt2f0tnIduYvHvKYuw6CYwqpCxcSpyO6TzWKagzWKo7zWL2XzWLFxzWLTOz0V
----out[256]----
00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57 58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77 78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87 88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97 98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7 A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7 B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7 C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7 D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7 E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7 F8 F9 FA FB FC FD FE FF
```

<a name="an61_javascript"></a>
## JavaScript AN61 예
```javascript
try {
    var src0 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可" ;
    print('src0['+src0.length+']:' + src0) ;
    var tmp0 = AN61.encode(src0) ;
    print('tmp0['+tmp0.length+']:' + tmp0) ;
    var out0 = AN61.decode(tmp0) ;
    print('out0['+out0.length+']:' + out0) ;
    print('src0 === out0 : ' + (src0 === out0)) ;

    // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
    var src1 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可🐘" ;    // Exception이 발생하는 경우
    print('src1['+src1.length+']:' + src1) ;
    try {
        var tmp1 = AN61.encode(src1) ;
        print("tmp1:" + tmp1) ;
        var out1 = AN61.decode(tmp1) ;
        print("out1:" + out1) ;
    }
    catch(e) {
        console.error(e) ;
        console.log('typeof Base62 : ' + (typeof Base62)) ;
        if(typeof Base62 === 'object') {
            var tmp2 = Base62.encode(AN61.toUTF8(src1)) ;
            print('tmp2['+tmp2.length+']:' + tmp2) ;
            var out2 = AN61.fromUTF8(Base62.decode(tmp2)) ;
            print('out2['+out2.length+']:' + out2) ;
            print('src1 === out2 : ' + (src1 === out2)) ;
        }
    }
} catch(e) {
    print(e) ;
}
```
-----------------------------------------------------------------------------------
```
src0[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
tmp0[76]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
out0[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
src0 === out0 : true
src1[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可🐘
invalid UCS2 character index 43 🐘
typeof Base62 : object
tmp2[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
out2[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可🐘
src1 === out2 : true
```

<a name="base62_python"></a>
## Python BASE62 예
```python
if __name__ == '__main__':
    bin = bytearray(256) ;
    for i in range(len(bin)) :
        bin[i] = i

    print('----bin['+str(len(bin))+']----')
    print(Base62.bin2hexa(bin))
    txt = Base62.encode(bin)
    print('txt['+str(len(txt))+']:'+txt)
    out = Base62.decode(txt)
    print('----out['+str(len(out))+']----')
    print(Base62.bin2hexa(out))
```
-----------------------------------------------------------------------------------
```
----bin[256]----
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF

txt[348]:003x0kgb1WKF2Hws33aW3oEA4Zqn5LUR67856rki7dOM8P209Aed9vIHAguuBSYYCECCCyopDkSTEW67FHikG3MOGo02HZcfILGJJ6swJrWaKdAELOmrMAQVMv49NggmOSKQPDx4PyahQkELRVqySHUcT38GTnktUZOXVL2BW6eoWrISXcv6YOYjZACNZup1agSebS6IcDivcyMZdk0DeVcqfHGUg2t8gnWlhZAPiKn3j6Qgjr4KkcgxlOKbm9xFmuasngEWoRrApDUnpy8Rqjl5rVOisH2Mt2f0tnIduYvHvKYuw6CYwqpCxcSpyO6TzWKagzWKo7zWL2XzWLFxzWLTOz0V
----out[256]----
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF
```

<a name="an61_python"></a>
## Python AN61 예
```python
if __name__ == '__main__':
    src0 = 'http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可'
    print('src0['+str(len(src0))+']:' + src0)
    an61__tmp0 = AN61.encode(src0)
    print('an61__tmp0['+str(len(an61__tmp0))+']:' + an61__tmp0)
    an61__out0 = AN61.decode(an61__tmp0)
    print('an61__out0['+str(len(an61__out0))+']:' + an61__out0)
    print('src0 == an61__out0 :', (src0 == an61__out0))
    base64_tmp = base64.b64encode(src0.encode('utf8')).decode('utf8')
    print('base64_tmp['+str(len(base64_tmp))+']:' + base64_tmp)
    base64_out = base64.b64decode(base64_tmp.encode('utf8')).decode('utf8')
    print('base64_out['+str(len(base64_out))+']:' + base64_out)

    # [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
    src1 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可🐘" # ValueError가 발생하는 경우
    print('src1['+str(len(src1))+']:' + src1)
    try :
        tmp1 = AN61.encode(src1)
        print('tmp1:' + tmp1)
        out1 = AN61.decode(tmp1)
        print('out1:' + out1)
    except ValueError as ve :
        print('Error : ' + str(ve))
        tmp2 = Base62.encode(src1.encode("utf8"))
        print('tmp2['+str(len(tmp2))+']:' + tmp2)
        out2 = Base62.decode(tmp2).decode("utf8")
        print('out2['+str(len(out2))+']:' + out2)
        print('src1 == out2 :', (src1 == out2))
```
-----------------------------------------------------------------------------------
```
src0[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
an61__tmp0[76]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y       
an61__out0[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
src0 == an61__out0 : True
base64_tmp[76]:aHR0cDovL3Rlc3QuY29tOjgwODAvYW42MS5kbz9uYW1lPeqwgOuCmOuLpCDjhLHjhLTigLsK5Y+v       
base64_out[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
src1[44]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可�🐘
Error : Illegal base62 character index 43 �🐘
tmp2[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U      
out2[44]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可�🐘
src1 == out2 : True
```

<a name="base62_csharp"></a>
## C# BASE62 예
```c#
static void Main(string[] args)
{
    byte[] bin = new byte[256];
    for (int i = 0, len = bin.Length; i < len; ++i)
        bin[i] = (byte)i;

    Console.WriteLine("----bin[" + bin.Length + "]----");
    Console.WriteLine(Base62.bin2hexa(bin));

    string txt = Base62.encode(bin);
    Console.WriteLine("txt[" + txt.Length + "]:" + txt);

    byte[] dst = Base62.decode(txt);
    Console.WriteLine("----dst[" + dst.Length + "]----");
    Console.WriteLine(Base62.bin2hexa(dst));
}
```
-----------------------------------------------------------------------------------
```
----bin[256]----
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF

txt[348]:003x0kgb1WKF2Hws33aW3oEA4Zqn5LUR67856rki7dOM8P209Aed9vIHAguuBSYYCECCCyopDkSTEW67FHikG3MOGo02HZcfILGJJ6swJrWaKdAELOmrMAQVMv49NggmOSKQPDx4PyahQkELRVqySHUcT38GTnktUZOXVL2BW6eoWrISXcv6YOYjZACNZup1agSebS6IcDivcyMZdk0DeVcqfHGUg2t8gnWlhZAPiKn3j6Qgjr4KkcgxlOKbm9xFmuasngEWoRrApDUnpy8Rqjl5rVOisH2Mt2f0tnIduYvHvKYuw6CYwqpCxcSpyO6TzWKagzWKo7zWL2XzWLFxzWLTOz0V
----dst[256]----
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF
```

<a name="an61_csharp"></a>
## C# AN61 예
```c#
static void Main(string[] args)
{
    string src0 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可";
    Console.WriteLine("src0[" + src0.Length + "]:" + src0);
    string an61__tmp0 = AN61.encode(src0);
    Console.WriteLine("an61__tmp0[" + an61__tmp0.Length + "]:" + an61__tmp0);
    string an61__out0 = AN61.decode(an61__tmp0);
    Console.WriteLine("an61__out0[" + an61__out0.Length + "]:" + an61__out0);
    Console.WriteLine("(src0 == an61__out0) : " + (src0 == an61__out0));

    string base64_tmp = Convert.ToBase64String(Encoding.UTF8.GetBytes(src0));
    Console.WriteLine("base64_tmp[" + base64_tmp.Length + "]:" + base64_tmp);
    string base64_out = Encoding.UTF8.GetString(Convert.FromBase64String(base64_tmp));
    Console.WriteLine("base64_out[" + base64_out.Length + "]:" + base64_out);

    // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
    string src1 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可🐘";    // ArgumentException이 발생하는 경우
    Console.WriteLine("src1[" + src1.Length + "]:" + src1);
    try
    {
        string tmp1 = AN61.encode(src1);
        Console.WriteLine("tmp1:" + tmp1);
        string out1 = AN61.decode(tmp1);
        Console.WriteLine("out1:" + out1);
    }
    catch (ArgumentException ae)
    {
        Console.Error.WriteLine("Error : " + ae.Message);

        string tmp2 = Base62.encode(Encoding.UTF8.GetBytes(src1));
        Console.WriteLine("tmp2[" + tmp2.Length + "]:" + tmp2);
        string out2 = Encoding.UTF8.GetString(Base62.decode(tmp2));
        Console.WriteLine("out2[" + out2.Length + "]:" + out2);
        Console.WriteLine("(src1 == out2) : " + (src1 == out2));
    }
}
```
-----------------------------------------------------------------------------------
```
src0[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
an61__tmp0[76]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
an61__out0[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
(src0 == an61__out0) : True
base64_tmp[76]:aHR0cDovL3Rlc3QuY29tOjgwODAvYW42MS5kbz9uYW1lPeqwgOuCmOuLpCDjhLHjhLTigLsK5Y+v
base64_out[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
src1[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可??
Error : invalid UCS2 character index 43 ??
tmp2[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
out2[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可??
(src1 == out2) : True
```

<a name="base62_cpp"></a>
## CPP BASE62 예
```cpp
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
```
-----------------------------------------------------------------------------------
```
----bin[256]----
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF

txt[348]:003x0kgb1WKF2Hws33aW3oEA4Zqn5LUR67856rki7dOM8P209Aed9vIHAguuBSYYCECCCyopDkSTEW67FHikG3MOGo02HZcfILGJJ6swJrWaKdAELOmrMAQVMv49NggmOSKQPDx4PyahQkELRVqySHUcT38GTnktUZOXVL2BW6eoWrISXcv6YOYjZACNZup1agSebS6IcDivcyMZdk0DeVcqfHGUg2t8gnWlhZAPiKn3j6Qgjr4KkcgxlOKbm9xFmuasngEWoRrApDUnpy8Rqjl5rVOisH2Mt2f0tnIduYvHvKYuw6CYwqpCxcSpyO6TzWKagzWKo7zWL2XzWLFxzWLTOz0V
----out[256]----
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF
```

<a name="an61_cpp_windows"></a>
## CPP AN61 예 (Windows)
```cpp
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
		std::wstring src0 = L"http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可" ;
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
		std::string src0 = "http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可" ;
		printf("src0[%zd]:%s\n", src0.length(), src0.c_str()) ;
		std::string tmp0 = AN61::encode(system2wstring(src0)) ;
		printf("tmp0:%s\n", tmp0.c_str()) ;
		std::string out0 = wstring2system(AN61::decode(tmp0)) ;
		printf("out0:%s\n", out0.c_str()) ;
		printf("src0.compare(out0) : %d\n", src0.compare(out0)) ;
	}
    
	{
		printf("---wstring to wstring---\n") ;
		// [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
		std::wstring src1 = L"http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可" ;
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
		// [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
		std::string utf8 = system2utf8("http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可") ;
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
```
-----------------------------------------------------------------------------------
```
locale : [Korean_Korea.949]
sizeof(wchar_t) : 2
---wstring to wstring---
src0[43]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
tmp0:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CixjSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
out0:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
src0.compare(out0) : 0
---EUC-KR to EUC-KR---
src0[50]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
tmp0:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CixjSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
out0:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
src0.compare(out0) : 0
---wstring to wstring---
src1[45]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可??
Error : invalid UCS2 character
tmp2[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CixjSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
out2[45]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可??
src1.compare(out2) : 0
---utf8 to utf8---
utf8[61]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可??
Error : invalid UCS2 character
tmp8[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CixjSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
out8[61]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可??
utf8.compare(out8) : 0
```

<a name="an61_cpp_linux"></a>
## CPP AN61 예 (Linux)
```cpp
// charset : UTF-8(on Linux)
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
		std::wstring src0 = L"http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可" ;
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
		std::string src0 = "http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可" ;
		printf("src0[%zd]:%s\n", src0.length(), src0.c_str()) ;
		std::string tmp0 = AN61::encode(system2wstring(src0)) ;
		printf("tmp0:%s\n", tmp0.c_str()) ;
		std::string out0 = wstring2system(AN61::decode(tmp0)) ;
		printf("out0:%s\n", out0.c_str()) ;
		printf("src0.compare(out0) : %d\n", src0.compare(out0)) ;
	}
    
	{
		printf("---wstring to wstring---\n") ;
		// [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
		std::wstring src1 = L"http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可🐘" ;
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
		// [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
		std::string utf8 = system2utf8("http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可🐘") ;
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

```
-----------------------------------------------------------------------------------
```
locale : [ko_KR.UTF-8]
sizeof(wchar_t) : 4
---wstring to wstring---
src0[43]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
tmp0:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CixjSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
out0:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
src0.compare(out0) : 0
---UTF8 to UTF8---
src0[57]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
tmp0:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CixjSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
out0:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
src0.compare(out0) : 0
---wstring to wstring---
src1[44]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可🐘
Error : invalid UCS2 character
tmp2[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CixjSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
out2[44]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可🐘
src1.compare(out2) : 0
---utf8 to utf8---
utf8[61]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可🐘
Error : invalid UCS2 character
tmp8[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CixjSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
out8[61]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可🐘
utf8.compare(out8) : 0
```

<a name="base62_pascal"></a>
## Pascal BASE62 예
```pascal
program Base62Test;

{$APPTYPE CONSOLE}

uses
  SysUtils, Base62;

function bin2hexa(bin : TBytes) : string ;
const
  HexaString = '0123456789ABCDEF' ;
var
  i : Integer ;
begin
  result := '' ;
  for i := 0 to Length(bin)-1 do
  begin
    result := result + HexaString[1 + (bin[i] shr 4)] ;
    result := result + HexaString[1 + (bin[i] and 15)] ;
    result := result + ' ' ;

    if (i mod 16) = 7 then
      result := result + ' '
    else if (i mod 16) = 15 then
      result := result + #10 ;
  end ;
end;

var
  bin, dst : TBytes ;
  txt : string ;
  i : Integer ;

begin
  SetLength(bin, 256) ;
  for i := 0 to Length(bin)-1 do
    bin[i] := i ;

  WriteLn('---bin[', Length(bin), ']---') ;
  WriteLn(bin2hexa(bin)) ;

  txt := TBase62.Encode(bin) ;
  WriteLn('txt[', Length(txt), ']:', txt) ;

  dst := TBase62.Decode(txt) ;
  WriteLn('---dst[', Length(dst), ']---') ;
  WriteLn(bin2hexa(dst)) ;
  ReadLn;
end.
```
-----------------------------------------------------------------------------------
```
---bin[256]---
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF

txt[348]:003x0kgb1WKF2Hws33aW3oEA4Zqn5LUR67856rki7dOM8P209Aed9vIHAguuBSYYCECCCyopDkSTEW67FHikG3MOGo02HZcfILGJJ6swJrWaKdAELOmrMAQVMv49NggmOSKQPDx4PyahQkELRVqySHUcT38GTnktUZOXVL2BW6eoWrISXcv6YOYjZACNZup1agSebS6IcDivcyMZdk0DeVcqfHGUg2t8gnWlhZAPiKn3j6Qgjr4KkcgxlOKbm9xFmuasngEWoRrApDUnpy8Rqjl5rVOisH2Mt2f0tnIduYvHvKYuw6CYwqpCxcSpyO6TzWKagzWKo7zWL2XzWLFxzWLTOz0V
---dst[256]---
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF
```

<a name="an61_pascal_delphi"></a>
## Pascal AN61 예 (Delphi)
```pascal
{ charset : EUC-KR }
program AN61Test;

{$APPTYPE CONSOLE}

uses
  SysUtils,
  AN61 in 'AN61.pas',
  Base62 in 'Base62.pas',
  ZString in 'ZString.pas';

var
  wsrc, wout : WideString ;
  wtmp, tmp0, tmp8 : string ;
  src0, out0 : AnsiString ;
  utf8, out8 : UTF8String ;
  
begin
  WriteLn('----------AnsiString------------') ;
  src0 := 'http://test.com:8080/an61.do?name=가나다 ㄱㄴ※'#10'可' ;  // Ansi
  WriteLn('src0[', Length(src0), ']:', src0) ;

  tmp0 := TAN61.Encode(AnsiToUtf8(src0)) ;
  WriteLn('tmp0[', Length(tmp0), ']:', tmp0) ;

  out0 := Utf8ToAnsi(TAN61.Decode(tmp0)) ;
  WriteLn('out0[', Length(out0), ']:', out0) ;

  WriteLn('src0 = out0 : ', (src0 = out0)) ;

  WriteLn('----------UTF8String----------') ;
  utf8 := AnsiToUtf8('http://test.com:8080/an61.do?name=가나다 ㄱㄴ※'#10'可') ;
  WriteLn('utf8[', Length(utf8), ']:', Utf8ToAnsi(utf8)) ;

  tmp8 := TAN61.Encode(utf8) ;
  WriteLn('tmp8[', Length(tmp8), ']:', tmp8) ;

  out8 := TAN61.Decode(tmp8) ;
  WriteLn('out8[', Length(out8), ']:', Utf8ToAnsi(out8)) ;

  WriteLn('utf8 = out8 : ', (utf8 = out8)) ;

  WriteLn('----------WideString----------') ;
  wsrc := 'http://test.com:8080/an61.do?name=가나다 ㄱㄴ※'#10'可' ;
  WriteLn('wsrc[', Length(wsrc), ']:', wsrc) ;

  wtmp := TAN61.Encode(wsrc) ;
  WriteLn('wtmp[', Length(wtmp), ']:', wtmp) ;

  wout := UTF8Decode(TAN61.Decode(wtmp)) ;
  WriteLn('wout[', Length(wout), ']:', wout) ;

  WriteLn('wsrc = wout : ', (wsrc = wout)) ;

  Writeln('----------UTF8String----------') ;
  // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
  utf8 := UTF8Encode('http://test.com:8080/an61.do?name=가나다 ㄱㄴ※'#10'可') ;
  utf8 := utf8 + #240 ; // 0xF0 #240
  utf8 := utf8 + #159 ; // 0x9F #159
  utf8 := utf8 + #144 ; // 0x90 #144
  utf8 := utf8 + #152 ; // 0x98 #152
  //WriteLn('utf8[', Length(utf8), ']:', UTF8Decode(utf8)) ; // 코끼리 때문에 안됨.
  WriteLn('utf8[', Length(utf8), ']:', TZString.SafeUTF8Decode(utf8)) ;

  try
    tmp8 := TAN61.Encode(utf8) ;
    WriteLn('tmp8[', Length(tmp8), ']:', tmp8) ;

    out8 := TAN61.Decode(tmp8) ;
    //WriteLn('utf8[', Length(out8), ']:', UTF8Decode(out8)) ; // 코끼리 때문에 안됨.
    WriteLn('utf8[', Length(out8), ']:', TZString.SafeUTF8Decode(out8)) ;
  except
    on e: Exception do
    begin
      WriteLn('Error :', e.Message) ;

      tmp8 := TBase62.Encode(TZString.Utf8ToBytes(utf8)) ;
      Writeln('tmp8[', Length(tmp8), ']:', tmp8) ;

      out8 := TZString.BytesToUtf8(TBase62.Decode(tmp8)) ;
      //WriteLn('utf8[', Length(out8), ']:', UTF8Decode(out8)) ; // 코끼리 때문에 안됨.
      WriteLn('out8[', Length(out8), ']:', TZString.SafeUTF8Decode(out8)) ;

      WriteLn('out8 = out8 : ', (out8 = out8)) ;
    end ;
  end ;

  WriteLn('----------WideString------------') ;
  // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
  wsrc := 'http://test.com:8080/an61.do?name=가나다 ㄱㄴ※'#10'可' ;
  wsrc := wsrc + #55357 ; // 0xD83D #55357
  wsrc := wsrc + #56344 ; // 0xDC18 #56344
  WriteLn('wsrc[', Length(wsrc), ']:', wsrc) ;

  try
    wtmp := TAN61.Encode(wsrc) ;
    WriteLn('wtmp[', Length(wtmp), ']:', wtmp) ;

    wout := UTF8Decode(TAN61.Decode(wtmp)) ;
    WriteLn('wout[', Length(wout), ']:', wout) ;
  except
    on e: Exception do
    begin
      WriteLn('Error :', e.Message) ;

      //wtmp := TBase62.Encode(TZString.StringToBytes(UTF8Encode(wsrc))) ;  // 코끼리 때문에 안됨.
      wtmp := TBase62.Encode(TZString.Utf8ToBytes(TZString.SafeUTF8Encode(wsrc))) ;
      WriteLn('wtmp[', Length(wtmp), ']:', wtmp) ;

      //wout := UTF8Decode(TZString.BytesToString(TBase62.Decode(wtmp))) ;  // 코끼리 때문에 안됨.
      wout := TZString.SafeUTF8Decode(TZString.BytesToUtf8(TBase62.Decode(wtmp))) ;
      WriteLn('wout[', Length(wout), ']:', wout) ;

      WriteLn('wsrc = wout : ', (wsrc = wout)) ;
    end ;
  end ;

  ReadLn;
end.
```
-----------------------------------------------------------------------------------
```
----------AnsiString------------
src0[50]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
tmp0[76]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
out0[50]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
src0 = out0 : TRUE
----------UTF8String----------
utf8[57]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
tmp8[76]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
out8[57]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
utf8 = out8 : TRUE
----------WideString----------
wsrc[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
wtmp[76]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
wout[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
wsrc = wout : TRUE
----------UTF8String----------
utf8[61]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可??
Error :invalid UCS2 Character
tmp8[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
out8[61]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可??
out8 = out8 : TRUE
----------WideString------------
wsrc[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可??
Error :invalid UCS2 Character
wtmp[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
wout[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可??
wsrc = wout : TRUE
```
<a name="an61_pascal_free_pascal"></a>
## Pascal AN61 예 (Free Pascal)
```pascal
{ charset : UTF-8 }
program AN61Test;

{$APPTYPE CONSOLE}

uses
  SysUtils, AN61, ZString, Base62 ;

var
  wsrc, wout : WideString ;
  wtmp, tmp8, tmp0 : string ;
  src0, out0 : AnsiString ;
  utf8, out8 : UTF8String ;

begin
  WriteLn('----------UTF8String----------') ;
  utf8 := AnsiToUtf8(UTf8ToAnsi('http://test.com:8080/an61.do?name=가나다 ㄱㄴ※'#10'可')) ;
  WriteLn('utf8[', Length(utf8), ']:', Utf8ToAnsi(utf8)) ;

  tmp8 := TAN61.Encode(utf8) ;
  WriteLn('tmp8[', Length(tmp8), ']:', tmp8) ;

  out8 := TAN61.Decode(tmp8) ;
  WriteLn('out8[', Length(out8), ']:', Utf8ToAnsi(out8)) ;

  WriteLn('utf8 = out8 : ', (utf8 = out8)) ;

  WriteLn('----------AnsiString------------') ;
  src0 := Utf8ToAnsi('http://test.com:8080/an61.do?name=가나다 ㄱㄴ※'#10'可') ;
  WriteLn('src0[', Length(src0), ']:', src0) ;

  tmp0 := TAN61.Encode(AnsiToUtf8(src0)) ;
  WriteLn('tmp0[', Length(tmp0), ']:', tmp0) ;

  out0 := Utf8ToAnsi(TAN61.Decode(tmp0)) ;
  WriteLn('out0[', Length(out0), ']:', out0) ;

  WriteLn('src0 = out0 : ', (src0 = out0)) ;

  WriteLn('----------WideString----------') ;
  wsrc := UTF8Decode('http://test.com:8080/an61.do?name=가나다 ㄱㄴ※'#10'可') ;
  WriteLn('wsrc[', Length(wsrc), ']:', wsrc) ;

  wtmp := TAN61.Encode(wsrc) ;
  WriteLn('wtmp[', Length(wtmp), ']:', wtmp) ;

  wout := UTF8Decode(TAN61.Decode(wtmp)) ;
  WriteLn('wout[', Length(wout), ']:', wout) ;

  WriteLn('wsrc = wout : ', (wsrc = wout)) ;

  Writeln('----------UTF8String----------') ;
  // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
  utf8 := TZString.SafeUTF8Encode(UTF8Decode('http://test.com:8080/an61.do?name=가나다 ㄱㄴ※'#10'可🐘')) ;
  WriteLn('utf8[', Length(utf8), ']:', UTF8Decode(utf8)) ;

  try
    tmp8 := TAN61.Encode(utf8) ;
    WriteLn('tmp8[', Length(tmp8), ']', tmp8) ;

    out8 := TAN61.Decode(tmp8) ;
    WriteLn('utf8[', Length(out8), ']:', UTF8Decode(out8)) ;
  except
    on e: Exception do
    begin
      WriteLn('Error :', e.Message) ;

      tmp8 := TBase62.Encode(TZString.Utf8ToBytes(utf8)) ;
      WriteLn('tmp8[', Length(tmp8), ']:', tmp8) ;

      out8 := TZString.BytesToUtf8(TBase62.Decode(tmp8)) ;
      WriteLn('out8[', Length(out8), ']:', UTF8Decode(out8)) ;

      WriteLn('utf8 = out8 : ', (utf8 = out8)) ;
    end ;
  end ;

  WriteLn('----------WideString------------') ;
  // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
  wsrc := UTF8Decode('http://test.com:8080/an61.do?name=가나다 ㄱㄴ※'#10'可🐘') ;
  WriteLn('wsrc[', Length(wsrc), ']:', wsrc) ;

  try
    wtmp := TAN61.Encode(wsrc) ;
    WriteLn('wtmp[', Length(wtmp), ']:', wtmp) ;
    wout := TZString.StringToWideString(TAN61.Decode(wtmp), 65001) ;
    WriteLn('wout[', Length(wout), ']:', wout) ;
  except
    on e: Exception do
    begin
      WriteLn('Error :', e.Message) ;

      wtmp := TBase62.Encode(TZString.StringToBytes(UTF8Encode(wsrc))) ;
      WriteLn('wtmp[', Length(wtmp), ']:', wtmp) ;

      wout := UTF8Decode(TZString.BytesToString(TBase62.Decode(wtmp))) ;
      WriteLn('wout[', Length(wout), ']:', wout) ;

      WriteLn('wsrc = wout : ', (wsrc = wout)) ;
    end ;
  end ;

  ReadLn;
end.
```
-----------------------------------------------------------------------------------
```
----------UTF8String----------
utf8[57]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
tmp8[76]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
out8[57]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
utf8 = out8 : TRUE
----------AnsiString------------
src0[50]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
tmp0[76]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
out0[50]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
src0 = out0 : TRUE
----------WideString----------
wsrc[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
wtmp[76]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
wout[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
wsrc = wout : TRUE
----------UTF8String----------
utf8[61]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可??
Error :invalid UCS2 Character
tmp8[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
out8[61]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可??
utf8 = out8 : TRUE
----------WideString------------
wsrc[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可??
Error :invalid UCS2 Character
wtmp[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
wout[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可??
wsrc = wout : TRUE
```

<a name="base62_php"></a>
## PHP BASE62 예
```php
<?php require_once 'base62.php' ?>
<!DOCTYPE html>
<html>
<body>
<code>
<?php
function toHexa(string $str) {
	$bin = unpack('C*', $str) ;
	$len = count($bin) ;
	$ret = "" ;
	for($i = 1;$i <= $len; ++$i) {
		$ret .= sprintf("%02X ", $bin[$i]) ;
		if($i % 16 == 0)
			$ret .= "\n" ;
		else if($i % 16 == 8)
			$ret .= " " ;
	}
	return $ret ;
}

try {
	$bin = "" ;
	for($i = 0; $i < 256; ++$i)
		$bin .= chr($i) ;
	printf("----\$bin[%d]----<br/>", strlen($bin)) ;
	printf("%s<br/>", nl2br(toHexa($bin))) ;
	$txt = Base62::encode($bin) ;
	printf("txt:%s<br/>", nl2br($txt)) ;
	$out = Base62::decode($txt) ;
	printf("----\$out[%d]----<br/>", strlen($out)) ;
	printf("%s<br/>", nl2br(toHexa($out))) ;
	
} catch(Exception $ex) {
	printf("Exception : %s<br/>", $ex->getMessage()) ;
}
?>
</code>
</body>
</html>
```
-----------------------------------------------------------------------------------
```
----$bin[256]----
00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57 58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77 78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87 88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97 98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7 A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7 B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7 C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7 D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7 E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7 F8 F9 FA FB FC FD FE FF

txt:003x0kgb1WKF2Hws33aW3oEA4Zqn5LUR67856rki7dOM8P209Aed9vIHAguuBSYYCECCCyopDkSTEW67FHikG3MOGo02HZcfILGJJ6swJrWaKdAELOmrMAQVMv49NggmOSKQPDx4PyahQkELRVqySHUcT38GTnktUZOXVL2BW6eoWrISXcv6YOYjZACNZup1agSebS6IcDivcyMZdk0DeVcqfHGUg2t8gnWlhZAPiKn3j6Qgjr4KkcgxlOKbm9xFmuasngEWoRrApDUnpy8Rqjl5rVOisH2Mt2f0tnIduYvHvKYuw6CYwqpCxcSpyO6TzWKagzWKo7zWL2XzWLFxzWLTOz0V
----$out[256]----
00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F
10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F
20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F
30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E 3F
40 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F
50 51 52 53 54 55 56 57 58 59 5A 5B 5C 5D 5E 5F
60 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F
70 71 72 73 74 75 76 77 78 79 7A 7B 7C 7D 7E 7F
80 81 82 83 84 85 86 87 88 89 8A 8B 8C 8D 8E 8F
90 91 92 93 94 95 96 97 98 99 9A 9B 9C 9D 9E 9F
A0 A1 A2 A3 A4 A5 A6 A7 A8 A9 AA AB AC AD AE AF
B0 B1 B2 B3 B4 B5 B6 B7 B8 B9 BA BB BC BD BE BF
C0 C1 C2 C3 C4 C5 C6 C7 C8 C9 CA CB CC CD CE CF
D0 D1 D2 D3 D4 D5 D6 D7 D8 D9 DA DB DC DD DE DF
E0 E1 E2 E3 E4 E5 E6 E7 E8 E9 EA EB EC ED EE EF
F0 F1 F2 F3 F4 F5 F6 F7 F8 F9 FA FB FC FD FE FF
 
```

<a name="an61_php"></a>
## PHP AN61 예
```php
<?php require_once 'an61.php' ?>
<?php require_once 'base62.php' ?>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
</head>
<body>
<code>
<?php
try {
	$src0 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可" ;
	printf("src0[%d] : %s<br/>", strlen($src0), nl2br($src0)) ;
	$an61__tmp0 = AN61::encode($src0) ;
	printf("an61__tmp0:%s<br/>", nl2br($an61__tmp0)) ;
	$an61__out0 = AN61::decode($an61__tmp0) ;
	printf("an61__out0:%s<br/>", nl2br($an61__out0)) ;
	$base64_tmp = base64_encode($src0) ;
	printf("base64_tmp:%s<br/>", nl2br($base64_tmp)) ;
	$base64_out = base64_decode($base64_tmp) ;
	printf("base64_out:%s<br/>", nl2br($base64_out)) ;
	
	// [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
	$src1 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可🐘" ;
	printf("src1[%d]:%s<br/>", strlen($src1), nl2br($src1)) ;
	try {
		$tmp1 = AN61::encode($src1) ;
		printf("tmp1:%s<br/>", nl2br($tmp1)) ;
		$out1 = AN61::decode($tmp1) ;
		printf("out1:%s<br/>", nl2br($out1)) ;
	}
	catch(Exception $e) {
		printf("Exception : %s<br/>", $e->getMessage()) ;
		
		$tmp2 = Base62::encode($src1) ;
		printf("tmp2:%s<br/>", nl2br($tmp2)) ;
		$out2 = Base62::decode($tmp2) ;
		printf("out2:%s<br/>", nl2br($out2)) ;
		
		if($src1 === $out2)	echo("src1 === out2<br/>") ;
	}
	
} catch(Exception $ex) {
	printf("Exception : %s<br/>", $ex->getMessage()) ;
}
?>
</code>
</body>
</html>
```
-----------------------------------------------------------------------------------
```
src0[57] : http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
an61__tmp0:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
an61__out0:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
base64_tmp:aHR0cDovL3Rlc3QuY29tOjgwODAvYW42MS5kbz9uYW1lPeqwgOuCmOuLpCDjhLHjhLTigLsK5Y+v
base64_out:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
src1[61]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可🐘
Exception : invalid UCS2 character
tmp2:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
out2:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可🐘
src1 === out2
```

<a name="base62_kotlin"></a>
## Kotlin BASE62 예
```kotlin
fun bin2hexa(bin: ByteArray): String {
    val ret = StringBuilder()
    for (i in bin.indices) {
        ret.append(String.format("%02X ", bin[i]))
        when (i % 16) {
            7 -> ret.append(" ")
            15 -> ret.append("\n")
        }
    }
    if (bin.size % 16 != 0) ret.append("\n")
    return ret.toString()
}

fun main(args: Array<String>) {
    try {
        val bin = ByteArray(256)
        for (i in bin.indices) bin[i] = i.toByte()
        println("----bin[" + bin.size + "]----")
        println(bin2hexa(bin))
        val txt = Base62.encode(bin)
        println("txt[" + txt.length + "]:" + txt)
        val out = Base62.decode(txt)
        println("----out[" + out.size + "]----")
        println(bin2hexa(out))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
```
-----------------------------------------------------------------------------------
```

----bin[256]----
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F 
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F 
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F 
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F 
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F 
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F 
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F 
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F 
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F 
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F 
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF 
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF 
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF 
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF 
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF 
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF 

txt[348]:003x0kgb1WKF2Hws33aW3oEA4Zqn5LUR67856rki7dOM8P209Aed9vIHAguuBSYYCECCCyopDkSTEW67FHikG3MOGo02HZcfILGJJ6swJrWaKdAELOmrMAQVMv49NggmOSKQPDx4PyahQkELRVqySHUcT38GTnktUZOXVL2BW6eoWrISXcv6YOYjZACNZup1agSebS6IcDivcyMZdk0DeVcqfHGUg2t8gnWlhZAPiKn3j6Qgjr4KkcgxlOKbm9xFmuasngEWoRrApDUnpy8Rqjl5rVOisH2Mt2f0tnIduYvHvKYuw6CYwqpCxcSpyO6TzWKagzWKo7zWL2XzWLFxzWLTOz0V
----out[256]----
00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F 
10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F 
20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F 
30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F 
40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F 
50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F 
60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F 
70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F 
80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F 
90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F 
A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF 
B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF 
C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF 
D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF 
E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF 
F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF 
 
```

<a name="an61_kotlin"></a>
## Kotlin AN61 예
```kotlin
import java.io.UnsupportedEncodingException
import java.util.*

fun main(args: Array<String>) {
    try {
        val src0 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可"
        println("src0[" + src0.length + "]:" + src0)
        val an61__tmp0: String = AN61.encode(src0)
        println("an61__tmp0[" + an61__tmp0.length + "]:" + an61__tmp0)
        val an61__out0: String = AN61.decode(an61__tmp0)
        println("an61__out0[" + an61__out0.length + "]:" + an61__out0)
        println("src0.equals(an61__out0) : " + (src0 == an61__out0))
        val base64_tmp = Base64.getEncoder().encodeToString(src0.toByteArray(charset("utf8")))
        println("base64_tmp[" + base64_tmp.length + "]:" + base64_tmp)
        val base64_out = String(Base64.getDecoder().decode(base64_tmp), charset("utf8"))
        println("base64_out[" + base64_out.length + "]:" + base64_out)

        // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
        val src1 = "http://test.com:8080/an61.do?name=가나다 ㄱㄴ※\n可🐘" // UnsupportedEncodingException이 발생하는 경우
        println("src1[" + src1.length + "]:" + src1) // String.length()은 문자갯수가 아니라, UTF16의 길이다.
        try {
            val tmp1: String = AN61.encode(src1)
            println("tmp1:$tmp1")
            val out1: String = AN61.decode(tmp1)
            println("out1:$out1")
        } catch (uee: UnsupportedEncodingException) {
            System.err.println(uee)
            val tmp2: String = Base62.encode(src1.toByteArray(charset("utf8")))
            println("tmp2[" + tmp2.length + "]:" + tmp2)
            val out2 = String(Base62.decode(tmp2), charset("utf8"))
            println("out2[" + out2.length + "]:" + out2)
            println("src1.equals(out2) : " + (src1 == out2))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
```
-----------------------------------------------------------------------------------
```
src0[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
an61__tmp0[76]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7Y
an61__out0[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
src0.equals(an61__out0) : true
base64_tmp[76]:aHR0cDovL3Rlc3QuY29tOjgwODAvYW42MS5kbz9uYW1lPeqwgOuCmOuLpCDjhLHjhLTigLsK5Y+v
base64_out[43]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可
src1[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可🐘
tmp2[83]:QVOZSTTLC33NTIeJPEfTElRKEFxJOid7CTUTSEKmOiZwFiOXWiaIco6jfdmdXfmjXfyWWfSTwG7YzIeAi2U
out2[45]:http://test.com:8080/an61.do?name=가나다 ㄱㄴ※
可🐘
src1.equals(out2) : true
java.io.UnsupportedEncodingException: invalid UCS2 character index 43 🐘
```