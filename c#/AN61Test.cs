using System;
using System.Text;

namespace Base62
{
    class AN61Test
    {
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
    }
}
