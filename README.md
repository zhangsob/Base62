# BASE62 (AN61 : AlphaNumeric61)
BASE64 Encode에는 +/= 기본형 또는 -_= URL형이 있다.  
여기서, 항상 특수문자가 문제가 되어. 0&#126;9, A&#126;Z, a&#126;z만으로 이루어진 Base62 (및 AN61 : AlphaNumeric61)을 만들어 보았다.

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
- java
- javascript
- python
- c#

## 예
- java (Base62)
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

- java (AN61)
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

- javascript (Base62)
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

- javascript (AN61)
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

- python (Base62)
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

- python (AN61)
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

## 예
- c# (Base62)
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

- c# (AN61)
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