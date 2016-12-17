/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class arkanoid
{
	
	
	
	static data_t gfxbank,palettebank;
	extern int arkanoid_paddle_select;
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Arkanoid has a three 512x4 palette PROMs (one per gun).
	  I don't know the exact values of the resistors between the RAM and the
	  RGB output. I assumed these values (the same as Commando)
	
	  bit 3 -- 220 ohm resistor  -- RED/GREEN/BLUE
	        -- 470 ohm resistor  -- RED/GREEN/BLUE
	        -- 1  kohm resistor  -- RED/GREEN/BLUE
	  bit 0 -- 2.2kohm resistor  -- RED/GREEN/BLUE
	
	***************************************************************************/
	public static VhConvertColorPromPtr arkanoid_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
			/* red component */
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* green component */
			bit0 = (color_prom[Machine.drv.total_colors] >> 0) & 0x01;
			bit1 = (color_prom[Machine.drv.total_colors] >> 1) & 0x01;
			bit2 = (color_prom[Machine.drv.total_colors] >> 2) & 0x01;
			bit3 = (color_prom[Machine.drv.total_colors] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* blue component */
			bit0 = (color_prom[2*Machine.drv.total_colors] >> 0) & 0x01;
			bit1 = (color_prom[2*Machine.drv.total_colors] >> 1) & 0x01;
			bit2 = (color_prom[2*Machine.drv.total_colors] >> 2) & 0x01;
			bit3 = (color_prom[2*Machine.drv.total_colors] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	} };
	
	
	
	public static WriteHandlerPtr arkanoid_d008_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bits 0 and 1 flip X and Y, I don't know which is which */
		flip_screen_x_w(offset, data & 0x01);
		flip_screen_y_w(offset, data & 0x02);
	
		/* bit 2 selects the input paddle */
	    arkanoid_paddle_select = data & 0x04;
	
		/* bit 3 is coin lockout (but not the service coin) */
		coin_lockout_w.handler(0, !(data & 0x08));
		coin_lockout_w.handler(1, !(data & 0x08));
	
		/* bit 4 is unknown */
	
		/* bits 5 and 6 control gfx bank and palette bank. They are used together */
		/* so I don't know which is which. */
		set_vh_global_attribute(&gfxbank, (data & 0x20) >> 5);
		set_vh_global_attribute(&palettebank, (data & 0x40) >> 6);
	
		/* bit 7 is unknown */
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr arkanoid_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		if (full_refresh != 0)
		{
			memset(dirtybuffer,1,videoram_size[0]);
		}
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 2;offs >= 0;offs -= 2)
		{
			int offs2;
	
			offs2 = offs/2;
			if (dirtybuffer[offs] || dirtybuffer[offs+1])
			{
				int sx,sy,code;
	
	
				dirtybuffer[offs] = 0;
				dirtybuffer[offs + 1] = 0;
	
				sx = offs2 % 32;
				sy = offs2 / 32;
	
				if (flip_screen_x != 0) sx = 31 - sx;
				if (flip_screen_y != 0) sy = 31 - sy;
	
				code = videoram.read(offs+1)+ ((videoram.read(offs)& 0x07) << 8) + 2048 * gfxbank;
				drawgfx(tmpbitmap,Machine.gfx[0],
						code,
						((videoram.read(offs)& 0xf8) >> 3) + 32 * palettebank,
						flip_screen_x,flip_screen_y,
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the temporary bitmap to the screen */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
	
		/* Draw the sprites. */
		for (offs = 0;offs < spriteram_size[0];offs += 4)
		{
			int sx,sy,code;
	
	
			sx = spriteram.read(offs);
			sy = 248 - spriteram.read(offs+1);
			if (flip_screen_x != 0) sx = 248 - sx;
			if (flip_screen_y != 0) sy = 248 - sy;
	
			code = spriteram.read(offs+3)+ ((spriteram.read(offs+2)& 0x03) << 8) + 1024 * gfxbank;
			drawgfx(bitmap,Machine.gfx[0],
					2 * code,
					((spriteram.read(offs+2)& 0xf8) >> 3) + 32 * palettebank,
					flip_screen_x,flip_screen_y,
					sx,sy + (flip_screen_y ? 8 : -8),
					&Machine.visible_area,TRANSPARENCY_PEN,0);
			drawgfx(bitmap,Machine.gfx[0],
					2 * code + 1,
					((spriteram.read(offs+2)& 0xf8) >> 3) + 32 * palettebank,
					flip_screen_x,flip_screen_y,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
