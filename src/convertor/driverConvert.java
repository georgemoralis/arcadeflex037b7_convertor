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
public class driverConvert {

    //for type field

    static final int GAMEDRIVER = 0;
    static final int MACHINEDRIVER = 1;
    static final int MEMORYREAD = 2;
    static final int MEMORYWRITE = 3;
    static final int IOREAD = 4;
    static final int IOWRITE = 5;
    static final int INPUTPORT = 6;
    static final int GFXLAYOUT = 10;
    static final int GFXDECODE = 11;
    static final int HILOAD = 12;
    static final int HISAVE = 13;
    static final int DRIVER_INIT = 14;
    static final int MEM_READ = 15;
    static final int MEM_WRITE = 16;
    static final int MACHINE_INTERRUPT = 17;
    static final int SN76496interface = 18;
    static final int DACinterface = 19;
    static final int MACHINE_INIT = 20;
    static final int YM3812interface = 21;
    static final int YM3526interface=22;
    static final int YMHANDLER=23;
    static final int ADPCM_INT=24;
    static final int MSM5205interface=25;
    static final int YM2203interface=26;
    static final int AY8910interface=27;

    //type2 fields
    static final int NEWINPUT = 12;
    static final int ROMDEF = 13;

    public static void Convertdriver() {
        Convertor.inpos = 0;//position of pointer inside the buffers
        Convertor.outpos = 0;

        boolean only_once_flag = false;//gia na baleis to header mono mia fora
        boolean line_change_flag = false;

        int kapa = 0;
        int i = 0;
        int type = 0;
        int type2 = 0;
        int i3 = -1;

        int[] insideagk = new int[10];//get the { that are inside functions
        int i8 = -1; //for checking ) in INPUT PORTS and ROM macros
        do {
            if (Convertor.inpos >= Convertor.inbuf.length)//an to megethos einai megalitero spase to loop
            {
                break;
            }
            char c = sUtil.getChar(); //pare ton character
            if (line_change_flag) {
                for (int i1 = 0; i1 < kapa; i1++) {
                    sUtil.putString("\t");
                }

                line_change_flag = false;
            }
            switch (c) {
                case 35: // '#'
                    if (!sUtil.getToken("#include"))//an den einai #include min to trexeis
                    {
                        break;
                    }
                    sUtil.skipLine();
                    if (!only_once_flag)//trekse auto to komati mono otan bris to proto include
                    {
                        only_once_flag = true;
                        sUtil.putString("/*\r\n");
                        sUtil.putString(" * ported to v" + Convertor.mameversion + "\r\n");
                        sUtil.putString(" * using automatic conversion tool v" + Convertor.convertorversion + "\r\n");
                        /*sUtil.putString(" * converted at : " + Convertor.timenow() + "\r\n");*/
                        sUtil.putString(" */ \r\n");
                        sUtil.putString("package drivers;\r\n");
                        sUtil.putString("\r\n");
                        sUtil.putString((new StringBuilder()).append("public class ").append(Convertor.className).append("\r\n").toString());
                        sUtil.putString("{\r\n");
                        kapa = 1;
                        line_change_flag = true;
                    }
                    continue;
                case 10: // '\n'
                    Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];
                    line_change_flag = true;
                    continue;
                case 's':
                    i = Convertor.inpos;

                    if (sUtil.getToken("static")) {
                        sUtil.skipSpace();
                    }
                    if (!sUtil.getToken("struct")) //an einai static alla oxi static struct
                    {
                        if (sUtil.getToken("int")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '(') {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            if (sUtil.getToken("void"))//an to soma tis function einai (void)
                            {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }

                                if (Convertor.token[0].contains("_interrupt")) {
                                    sUtil.putString((new StringBuilder()).append("public static InterruptPtr ").append(Convertor.token[0]).append(" = new InterruptPtr() { public int handler() ").toString());
                                    type = MACHINE_INTERRUPT;
                                    i3 = -1;
                                    continue;
                                }

                            }
                            sUtil.skipSpace();
                            if (sUtil.getToken("int")) {
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken();
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                
                                sUtil.skipSpace();
                                if (Convertor.token[0].length() > 0 && Convertor.token[1].length() > 0) {
                                    sUtil.putString((new StringBuilder()).append("public static ReadHandlerPtr ").append(Convertor.token[0]).append(" = new ReadHandlerPtr() { public int handler(int ").append(Convertor.token[1]).append(")").toString());
                                    type = MEM_READ;
                                    i3 = -1;
                                    continue;
                                }

                            }
                            Convertor.inpos = i;
                            break;
                        }
                        if (sUtil.getToken("void")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '(') {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            if (sUtil.getToken("void"))//an to soma tis function einai (void)
                            {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                

                                if (Convertor.token[0].contains("hisave")) {
                                    sUtil.putString((new StringBuilder()).append("static HiscoreSavePtr ").append(Convertor.token[0]).append(" = new HiscoreSavePtr() { public void handler() ").toString());
                                    type = HISAVE;
                                    i3 = -1;
                                    continue;
                                }
                                if (Convertor.token[0].startsWith("init_")) {
                                    sUtil.putString((new StringBuilder()).append("public static InitDriverPtr ").append(Convertor.token[0]).append(" = new InitDriverPtr() { public void handler() ").toString());
                                    type = DRIVER_INIT;
                                    i3 = -1;
                                    continue;
                                }
                                if (Convertor.token[0].contains("machine_init") || Convertor.token[0].contains("_machine_init") || Convertor.token[0].contains("_init_machine")) {
                                    sUtil.putString((new StringBuilder()).append("public static InitMachinePtr ").append(Convertor.token[0]).append(" = new InitMachinePtr() { public void handler() ").toString());
                                    type = MACHINE_INIT;
                                    i3 = -1;
                                    continue;
                                }
                                
                            }
                            if (!sUtil.getToken("int")) {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            Convertor.token[1] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if(Convertor.token[0].contains("irqhandler"))
                            {
                                sUtil.putString((new StringBuilder()).append("public static WriteYmHandlerPtr ").append(Convertor.token[0]).append(" = new WriteYmHandlerPtr() { public void handler(int ").append(Convertor.token[1]).toString());
                                type=YMHANDLER;
                                i3=-1;
                                continue;
                            }
                            if(Convertor.token[0].contains("adpcm_int"))
                            {
                                sUtil.putString((new StringBuilder()).append("public static vclk_interruptPtr ").append(Convertor.token[0]).append(" = new vclk_interruptPtr() { public void handler(int ").append(Convertor.token[1]).toString());
                                type=ADPCM_INT;
                                i3=-1;
                                continue;
                            }
                            if (sUtil.parseChar() != ',') {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            if (!sUtil.getToken("int")) {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            Convertor.token[2] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != ')') {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            if (Convertor.token[0].length() > 0 && Convertor.token[1].length() > 0 && Convertor.token[2].length() > 0) {
                                sUtil.putString((new StringBuilder()).append("public static WriteHandlerPtr ").append(Convertor.token[0]).append(" = new WriteHandlerPtr() { public void handler(int ").append(Convertor.token[1]).append(", int ").append(Convertor.token[2]).append(")").toString());
                                type = MEM_WRITE;
                                i3 = -1;
                                continue;
                            }
                            

                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.inpos = i;
                    } else {
                        sUtil.skipSpace();
                        if (sUtil.getToken("GameDriver")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("public static GameDriver " + Convertor.token[0] + " = new GameDriver");
                                type = GAMEDRIVER;
                                i3 = -1;
                                continue;
                            }
                        }  else if (sUtil.getToken("MemoryReadAddress")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '[') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ']') {
                                    Convertor.inpos = i;
                                } else {
                                    sUtil.skipSpace();
                                    if (sUtil.parseChar() != '=') {
                                        Convertor.inpos = i;
                                    } else {
                                        sUtil.skipSpace();
                                        sUtil.putString("static MemoryReadAddress " + Convertor.token[0] + "[] =");
                                        type = MEMORYREAD;
                                        i3 = -1;
                                        continue;
                                    }
                                }
                            }
                        } else if (sUtil.getToken("MemoryWriteAddress")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '[') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ']') {
                                    Convertor.inpos = i;
                                } else {
                                    sUtil.skipSpace();
                                    if (sUtil.parseChar() != '=') {
                                        Convertor.inpos = i;
                                    } else {
                                        sUtil.skipSpace();
                                        sUtil.putString("static MemoryWriteAddress " + Convertor.token[0] + "[] =");
                                        type = MEMORYWRITE;
                                        i3 = -1;
                                        continue;
                                    }
                                }
                            }
                        } else if (sUtil.getToken("IOReadPort")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '[') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ']') {
                                    Convertor.inpos = i;
                                } else {
                                    sUtil.skipSpace();
                                    if (sUtil.parseChar() != '=') {
                                        Convertor.inpos = i;
                                    } else {
                                        sUtil.skipSpace();
                                        sUtil.putString("static IOReadPort " + Convertor.token[0] + "[] =");
                                        type = IOREAD;
                                        i3 = -1;
                                        continue;
                                    }
                                }
                            }
                        } else if (sUtil.getToken("IOWritePort")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '[') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ']') {
                                    Convertor.inpos = i;
                                } else {
                                    sUtil.skipSpace();
                                    if (sUtil.parseChar() != '=') {
                                        Convertor.inpos = i;
                                    } else {
                                        sUtil.skipSpace();
                                        sUtil.putString("static IOWritePort " + Convertor.token[0] + "[] =");
                                        type = IOWRITE;
                                        i3 = -1;
                                        continue;
                                    }
                                }
                            }
                        }

                          else {
                            Convertor.inpos = i;
                        }
                    }
                    break;
                case '{':
                    if (type == GAMEDRIVER) {
                        i3++;
                        insideagk[i3] = 0;
                        Convertor.outbuf[(Convertor.outpos++)] = '(';
                        Convertor.inpos += 1;
                        continue;
                    }
                     else if (type == MEMORYREAD) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new MemoryReadAddress(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == MEMORYWRITE) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new MemoryWriteAddress(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == IOREAD) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new IOReadPort(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == IOWRITE) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new IOWritePort(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == GFXDECODE) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new GfxDecodeInfo(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }

                    else if (type == DACinterface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }  else if (type == DRIVER_INIT || type == MEM_READ || type == MEM_WRITE ||  type == ADPCM_INT || type == YMHANDLER || type == MACHINE_INTERRUPT || type == MACHINE_INIT) {
                        i3++;
                    }
                    break;
                case '}':
                    if (type == GAMEDRIVER) {
                        i3--;
                        Convertor.outbuf[(Convertor.outpos++)] = ')';
                        Convertor.inpos += 1;
                        type = -1;
                        continue;
                    }
                    if (type == DRIVER_INIT || type == MEM_READ || type == MEM_WRITE || type==ADPCM_INT || type == YMHANDLER || type == MACHINE_INTERRUPT || type == MACHINE_INIT) {
                        i3--;
                        if (i3 == -1) {
                            sUtil.putString("} };");
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                     else if ((type == MEMORYREAD) || (type == MEMORYWRITE) || (type == IOREAD) || (type == IOWRITE) || (type == GFXDECODE)) {
                        i3--;
                        if (i3 == -1) {
                            type = -1;
                        } else if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = ')';
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == GFXLAYOUT) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    } else if (type == SN76496interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }

                    else if (type == DACinterface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    break;
                case 'e':
                    if (sUtil.getToken("extern"))//if it starts with extern skip it
                    {
                        sUtil.skipLine();
                        continue;
                    }
                    i = Convertor.inpos;
                    if (sUtil.getToken("enum")) {
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != '{') {
                            Convertor.inpos = i;
                        } else {
                            sUtil.skipSpace();
                            int i5 = 0;
                            do {
                                Convertor.token[(i5++)] = sUtil.parseToken();
                                sUtil.skipSpace();
                                c = sUtil.parseChar();
                                if ((c != '}') && (c != ',')) {
                                    Convertor.inpos = i;
                                    break;
                                }
                                sUtil.skipSpace();
                            } while (c == ',');
                            if (sUtil.parseChar() != ';') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.putString("static final int ");
                                for (int i6 = 0; i6 < i5; i6++) {
                                    sUtil.putString(Convertor.token[i6] + " = " + i6);
                                    sUtil.putString(i6 == i5 - 1 ? ";" : ", ");
                                }
                                continue;
                            }
                        }
                    } else {
                        i = Convertor.inpos;
                    }

                    break;
                case '&':
                    if (type == GAMEDRIVER) {
                        Convertor.inpos += 1;
                        continue;
                    }
                    if (type == MEMORYREAD || type == MEMORYWRITE || type == IOREAD || type == IOWRITE) {
                        Convertor.inpos += 1;
                        continue;
                    }
                    if (type == GFXDECODE) {
                        Convertor.inpos += 1;
                        continue;
                    }

                    break;
                case 'u':
                    
                    if (type == MEM_WRITE || type == DRIVER_INIT) {
                        if(i3==-1) break;//if is not inside a memwrite function break
                        i=Convertor.inpos;
                        if (sUtil.getToken("unsigned char")) {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '*') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.putString((new StringBuilder()).append("UBytePtr ").toString());
                                continue;
                            }
                        }
                    }

                    break;
                case 'o':
                    if (type == HILOAD || type == HISAVE) {
                        i = Convertor.inpos;
                        if (sUtil.getToken("osd_fread(f")) {
                            if (sUtil.parseChar() != ',') {
                                Convertor.inpos = i;
                            } else {
                                if (sUtil.parseChar() != '&') {
                                    Convertor.inpos = i;
                                } else {
                                    Convertor.token[0] = sUtil.parseToken();
                                    if (Convertor.token[0].contains("+"))//3stooges has RAM+offset ingnore conversion
                                    {
                                        Convertor.inpos = i;
                                        continue;
                                    }
                                    if (sUtil.parseChar() != '[') {
                                        Convertor.inpos = i;
                                    } else {
                                        //sUtil.skipSpace();
                                        Convertor.token[1] = sUtil.parseToken();
                                        //sUtil.skipSpace();
                                        if (sUtil.parseChar() != ']') {
                                            Convertor.inpos = i;
                                        } else {
                                            sUtil.skipSpace();
                                            sUtil.putString("osd_fread(f," + Convertor.token[0] + ", " + Convertor.token[1]);
                                            continue;
                                        }
                                    }
                                }
                            }
                        }
                        if (sUtil.getToken("osd_fwrite(f")) {
                            if (sUtil.parseChar() != ',') {
                                Convertor.inpos = i;
                            } else {
                                if (sUtil.parseChar() != '&') {
                                    Convertor.inpos = i;
                                } else {
                                    Convertor.token[0] = sUtil.parseToken();
                                    if (Convertor.token[0].contains("+"))//3stooges has RAM+offset ingnore conversion
                                    {
                                        Convertor.inpos = i;
                                        continue;
                                    }
                                    if (sUtil.parseChar() != '[') {
                                        Convertor.inpos = i;
                                    } else {
                                        //sUtil.skipSpace();
                                        Convertor.token[1] = sUtil.parseToken();
                                        //sUtil.skipSpace();
                                        if (sUtil.parseChar() != ']') {
                                            Convertor.inpos = i;
                                        } else {
                                            sUtil.skipSpace();
                                            sUtil.putString("osd_fwrite(f," + Convertor.token[0] + ", " + Convertor.token[1]);
                                            continue;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;

            }

            Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];//grapse to inputbuffer sto output
        } while (true);
        if (only_once_flag) {
            sUtil.putString("}\r\n");
        }
    }
}
