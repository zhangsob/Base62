using System;

namespace Base62
{
    class Base62Test
    {
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
    }
}
