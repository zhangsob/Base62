// for node
const Base62 = require('./Base62.js') ;

function print(msg) {
    if(typeof document !== 'undefined' && typeof msg === 'string')
        document.write(msg.replace(/\n/g,'<br/>') + '<br/>') ;
    console.log(msg) ;
}

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
