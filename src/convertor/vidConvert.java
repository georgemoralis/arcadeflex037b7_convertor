/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
package convertor;

/**
 *
 * @author george
 */
public class vidConvert {
    static final int vid_mem_read=20;
    static final int vid_mem_write=21;
    static final int vh_stop=22;
    static final int vh_screenrefresh=23;
    static final int vh_convert=24;
    static final int vh_start=25;
    static final int spriteram=26;
    
    
    public static void ConvertVideo()
    {
         Convertor.inpos = 0;//position of pointer inside the buffers
        Convertor.outpos = 0;
        
        boolean only_once_flag=false;//gia na baleis to header mono mia fora
        boolean line_change_flag=false;
        int type=0;
        int l=0;
        
        int k=0;
        
        
label0: 
        do
        {
            if(Convertor.inpos >= Convertor.inbuf.length)//an to megethos einai megalitero spase to loop
            {
                break;
            }
            char c = sUtil.getChar(); //pare ton character
            if(line_change_flag)
            {
                for(int i1 = 0; i1 < k; i1++)
                {
                    sUtil.putString("\t");
                }

                line_change_flag = false;
            }
            switch(c)
            {
              case 's':
                       /* if(sUtil.parseChar() != '[')
                        {
                            Convertor.inpos = r;
                            break;
                        }
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                         sUtil.skipSpace();
                        if(sUtil.parseChar() != ']')
                        {
                            Convertor.inpos = r;
                            break;
                        }
                        sUtil.skipSpace();
                        //if(sUtil.parseChar() != '=')
                           sUtil.putString((new StringBuilder()).append("spriteram.read(").append(Convertor.token[0]).append(")").toString()); 
                        //else
                          //  Convertor.inpos = r;*/
                  break;
              case 35: // '#'
                if(!sUtil.getToken("#include"))//an den einai #include min to trexeis
                {
                    break;
                }
                sUtil.skipLine();
                if(!only_once_flag)//trekse auto to komati mono otan bris to proto include
                {
                    only_once_flag = true;
                    sUtil.putString("/*\r\n");
                    sUtil.putString(" * ported to v" + Convertor.mameversion + "\r\n");
                    sUtil.putString(" * using automatic conversion tool v" + Convertor.convertorversion + "\r\n");
                    /*sUtil.putString(" * converted at : " + Convertor.timenow() + "\r\n");*/
                    sUtil.putString(" *\r\n");
                    sUtil.putString(" *\r\n");
                    sUtil.putString(" *\r\n");
                    sUtil.putString(" */ \r\n");
                    sUtil.putString("package vidhrdw;\r\n");
                    sUtil.putString("\r\n");
                    //add a few common used imports
                    sUtil.putString("import static arcadeflex.libc.*;\r\n");
                    sUtil.putString("import static mame.drawgfxH.*;\r\n");
                    sUtil.putString("import static mame.drawgfx.*;\r\n");
                    sUtil.putString("import static vidhrdw.generic.*;\r\n");
                    sUtil.putString("import static mame.driverH.*;\r\n");
                    sUtil.putString("import static mame.osdependH.*;\r\n");
                    sUtil.putString("import static mame.mame.*;\r\n");
                    sUtil.putString("\r\n");
                    sUtil.putString((new StringBuilder()).append("public class ").append(Convertor.className).append("\r\n").toString());
                    sUtil.putString("{\r\n");
                    k=1;
                    line_change_flag = true;
                }
                continue;
              case 10: // '\n'
                Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];
                line_change_flag = true;
                continue;
             case 45: // '-'
                char c3 = sUtil.getNextChar();
                if(c3 != '>')
                {
                    break;
                }
                Convertor.outbuf[Convertor.outpos++] = '.';
                Convertor.inpos += 2;
                continue;  
            case 105: // 'i'

             case 118: // 'v'
                    int j = Convertor.inpos;
                    if(!sUtil.getToken("void"))
                    {
                        break;
                    }
                    sUtil.skipSpace();
                    Convertor.token[0] = sUtil.parseToken();
                    
                    sUtil.skipSpace();
                    if(sUtil.parseChar() != '(')
                    {
                        Convertor.inpos = j;
                        break;
                    }
                    sUtil.skipSpace();
                    if(sUtil.getToken("struct osd_bitmap *bitmap,int full_refresh"))
                    {
                        sUtil.skipSpace();
                        if(sUtil.parseChar() != ')')
                        {
                            Convertor.inpos = j;
                            break;
                        }
                        if(Convertor.token[0].contains("vh_screenrefresh"))
                        {
                            sUtil.putString((new StringBuilder()).append("public static VhUpdatePtr ").append(Convertor.token[0]).append(" = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) ").toString());
                            type = vh_screenrefresh;
                            l = -1;
                            continue label0; //ξαναργυρνα στην αρχη για να μην γραψεις και την παλια συνάρτηση
                        }   
                        
                    }
                    if(sUtil.getToken("unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom"))
                    {
                        if(sUtil.parseChar() != ')')
                        {
                            Convertor.inpos = j;
                            break;
                        }
                        if(Convertor.token[0].contains("vh_convert_color_prom"))
                        {
                            sUtil.putString((new StringBuilder()).append("public static VhConvertColorPromPtr ").append(Convertor.token[0]).append(" = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) ").toString());
                            type = vh_convert;
                            l = -1;
                            continue label0; //ξαναργυρνα στην αρχη για να μην γραψεις και την παλια συνάρτηση
                        }   
                        
                    }                  


                    Convertor.inpos = j;           
                    break;
             case 123: // '{'
                    l++;
                break;
             case 125: // '}'
                l--;
                if(type != vid_mem_read && type != vid_mem_write  && type!=vh_stop && type!=vh_start && type!=vh_screenrefresh && type!=vh_convert || l != -1)
                {
                    break;
                }
                sUtil.putString("} };");
                Convertor.inpos++;
                type = -1;
                continue; 
            }
  
            
            Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];//grapse to inputbuffer sto output
        }while(true);
        if(only_once_flag)
        {
            sUtil.putString("}\r\n");
        }
       
    }   
}
