/***************************************************************************

	vidhrdw.c

	Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class tp84
{
	
	
	
	UBytePtr tp84_videoram2;
	UBytePtr tp84_colorram2;
	static struct osd_bitmap *tmpbitmap2;
	static UBytePtr dirtybuffer2;
	
	UBytePtr tp84_scrollx;
	UBytePtr tp84_scrolly;
	
	int col0;
	
	
	
	
	static struct rectangle topvisiblearea =
	{
		0*8, 2*8-1,
		2*8, 30*8-1
	};
	static struct rectangle bottomvisiblearea =
	{
		30*8, 32*8-1,
		2*8, 30*8-1
	};
	
	
	
	/*
	-The colortable is divided in 2 part:
	 -The characters colors
	 -The sprites colors
	
	-The characters colors are indexed like this:
	 -2 bits from the characters
	 -4 bits from the attribute in colorram
	 -2 bits from col0 (d3-d4)
	 -3 bits from col0 (d0-d1-d2)
	-So, there is 2048 bytes for the characters
	
	-The sprites colors are indexed like this:
	 -4 bits from the sprites (16 colors)
	 -4 bits from the attribute of the sprites
	 -3 bits from col0 (d0-d1-d2)
	-So, there is 2048 bytes for the sprites
	
	*/
	/*
		 The RGB signals are generated by 3 proms 256X4 (prom 2C, 2D and 1E)
			The resistors values are:
				1K  ohm
				470 ohm
				220 ohm
				100 ohm
	*/
	public static VhConvertColorPromPtr tp84_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
			/* red component */
			bit0 = (color_prom[0] >> 0) & 0x01;
			bit1 = (color_prom[0] >> 1) & 0x01;
			bit2 = (color_prom[0] >> 2) & 0x01;
			bit3 = (color_prom[0] >> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x42 * bit2 + 0x90 * bit3;
			/* green component */
			bit0 = (color_prom[Machine.drv.total_colors] >> 0) & 0x01;
			bit1 = (color_prom[Machine.drv.total_colors] >> 1) & 0x01;
			bit2 = (color_prom[Machine.drv.total_colors] >> 2) & 0x01;
			bit3 = (color_prom[Machine.drv.total_colors] >> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x42 * bit2 + 0x90 * bit3;
			/* blue component */
			bit0 = (color_prom[2*Machine.drv.total_colors] >> 0) & 0x01;
			bit1 = (color_prom[2*Machine.drv.total_colors] >> 1) & 0x01;
			bit2 = (color_prom[2*Machine.drv.total_colors] >> 2) & 0x01;
			bit3 = (color_prom[2*Machine.drv.total_colors] >> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x42 * bit2 + 0x90 * bit3;
	
			color_prom++;
		}
	
		color_prom += 2*Machine.drv.total_colors;
		/* color_prom now points to the beginning of the lookup table */
	
	
		/* characters use colors 128-255 */
		for (i = 0;i < TOTAL_COLORS(0)/8;i++)
		{
			int j;
	
	
			for (j = 0;j < 8;j++)
				COLOR(0,i+256*j) = *color_prom + 128 + 16*j;
	
			color_prom++;
		}
	
		/* sprites use colors 0-127 */
		for (i = 0;i < TOTAL_COLORS(1)/8;i++)
		{
			int j;
	
	
			for (j = 0;j < 8;j++)
			{
				if (*color_prom) COLOR(1,i+256*j) = *color_prom + 16*j;
				else COLOR(1,i+256*j) = 0;	/* preserve transparency */
			}
	
			color_prom++;
		}
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr tp84_vh_start = new VhStartPtr() { public int handler() 
	{
		if (generic_vh_start() != 0)
			return 1;
	
		if ((dirtybuffer2 = malloc(videoram_size)) == 0)
		{
			generic_vh_stop();
			return 1;
		}
		memset(dirtybuffer2,1,videoram_size);
	
		if ((tmpbitmap2 = bitmap_alloc(Machine.drv.screen_width,Machine.drv.screen_height)) == 0)
		{
			free(dirtybuffer2);
			generic_vh_stop();
			return 1;
		}
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr tp84_vh_stop = new VhStopPtr() { public void handler() 
	{
		free(dirtybuffer2);
		bitmap_free(tmpbitmap2);
		generic_vh_stop();
	} };
	
	
	
	public static WriteHandlerPtr tp84_videoram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (tp84_videoram2[offset] != data)
		{
			dirtybuffer2[offset] = 1;
	
			tp84_videoram2[offset] = data;
		}
	} };
	
	
	
	public static WriteHandlerPtr tp84_colorram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (tp84_colorram2[offset] != data)
		{
			dirtybuffer2[offset] = 1;
	
			tp84_colorram2[offset] = data;
		}
	} };
	
	
	
	/*****
	  col0 is a register to index the color Proms
	*****/
	public static WriteHandlerPtr tp84_col0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if(col0 != data)
		{
			col0 = data;
	
			memset(dirtybuffer,1,videoram_size);
			memset(dirtybuffer2,1,videoram_size);
		}
	} };
	
	
	
	/***************************************************************************
	
		Draw the game screen in the given osd_bitmap.
		Do NOT call osd_update_display() from this function, it will be called by
		the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr tp84_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
		int coloffset;
	
	
		coloffset = ((col0&0x18) << 1) + ((col0&0x07) << 6);
	
		for (offs = videoram_size - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs)+ ((colorram.read(offs)& 0x30) << 4),
						(colorram.read(offs)& 0x0f) + coloffset,
						colorram.read(offs)& 0x40,colorram.read(offs)& 0x80,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
	
			if (dirtybuffer2[offs])
			{
				int sx,sy;
	
	
				dirtybuffer2[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
			/* Skip the middle of the screen, this ram seem to be used as normal ram. */
				if (sx < 2 || sx >= 30)
					drawgfx(tmpbitmap2,Machine.gfx[0],
							tp84_videoram2[offs] + ((tp84_colorram2[offs] & 0x30) << 4),
							(tp84_colorram2[offs] & 0x0f) + coloffset,
							tp84_colorram2[offs] & 0x40,tp84_colorram2[offs] & 0x80,
							8*sx,8*sy,
							&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the temporary bitmap to the screen */
		{
			int scrollx,scrolly;
	
	
			scrollx = -*tp84_scrollx;
			scrolly = -*tp84_scrolly;
	
			copyscrollbitmap(bitmap,tmpbitmap,1,&scrollx,1,&scrolly,&Machine.visible_area,TRANSPARENCY_NONE,0);
		}
	
		/* Draw the sprites. */
		coloffset = ((col0&0x07) << 4);
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			int sx,sy,flipx,flipy;
	
	
			sx = spriteram.read(offs+0);
			sy = 240-spriteram.read(offs+3);
			flipx = !(spriteram.read(offs+2)& 0x40);
			flipy = spriteram.read(offs+2)& 0x80;
	
			drawgfx(bitmap,Machine.gfx[1],
					spriteram.read(offs+1),
					(spriteram.read(offs+2)& 0x0f) + coloffset,
					flipx,flipy,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_COLOR,0);
		}
	
	
		/* Copy the frontmost playfield. */
		copybitmap(bitmap,tmpbitmap2,0,0,0,0,&topvisiblearea,TRANSPARENCY_NONE,0);
		copybitmap(bitmap,tmpbitmap2,0,0,0,0,&bottomvisiblearea,TRANSPARENCY_NONE,0);
	} };
}
