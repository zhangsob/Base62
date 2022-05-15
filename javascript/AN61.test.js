// for node
const AN61 = require('./AN61.js') ;

function print(msg) {
    if(typeof document !== 'undefined' && typeof msg === 'string')
        document.write(msg.replace(/\n/g,'<br/>') + '<br/>') ;
    console.log(msg) ;
}

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
        const Base62 = require('./Base62.js') ;
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
