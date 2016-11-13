/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class srumbler
{
	
	
	unsigned char *srumbler_backgroundram,*srumbler_foregroundram;
	static struct tilemap *bg_tilemap,*fg_tilemap;
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	static void get_fg_tile_info(int tile_index)
	{
		unsigned char attr = srumbler_foregroundram[2*tile_index];
		SET_TILE_INFO(0,srumbler_foregroundram[2*tile_index + 1] + ((attr & 0x03) << 8),(attr & 0x3c) >> 2)
		tile_info.flags = (attr & 0x40) ? TILE_IGNORE_TRANSPARENCY : 0;
	}
	
	static void get_bg_tile_info(int tile_index)
	{
		unsigned char attr = srumbler_backgroundram[2*tile_index];
		SET_TILE_INFO(1,srumbler_backgroundram[2*tile_index + 1] + ((attr & 0x07) << 8),(attr & 0xe0) >> 5)
		tile_info.flags = TILE_SPLIT((attr & 0x10) >> 4);
		if ((attr & 0x08) != 0) tile_info.flags |= TILE_FLIPY;
	}
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr srumbler_vh_start = new VhStartPtr() { public int handler() 
	{
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_cols,TILEMAP_TRANSPARENT,8,8,64,32);
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_cols,TILEMAP_SPLIT,    16,16,64,64);
	
		if (!fg_tilemap || !bg_tilemap)
			return 1;
	
		fg_tilemap.transparent_pen = 3;
	
		bg_tilemap.transmask[0] = 0xffff; /* split type 0 is totally transparent in front half */
		bg_tilemap.transmask[1] = 0x07ff; /* split type 1 has pens 0-10 transparent in front half */
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr srumbler_foreground_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (srumbler_foregroundram[offset] != data)
		{
			srumbler_foregroundram[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap,offset/2);
		}
	} };
	
	public static WriteHandlerPtr srumbler_background_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (srumbler_backgroundram[offset] != data)
		{
			srumbler_backgroundram[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap,offset/2);
		}
	} };
	
	
	public static WriteHandlerPtr srumbler_4009_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bit 0 flips screen */
		flip_screen_w(0,data & 1);
	
		/* bits 4-5 used during attract mode, unknown */
	
		/* bits 6-7 coin counters */
		coin_counter_w.handler(0,data & 0x40);
		coin_counter_w.handler(1,data & 0x80);
	} };
	
	
	public static WriteHandlerPtr srumbler_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int scroll[4];
	
		scroll[offset] = data;
	
		tilemap_set_scrollx(bg_tilemap,0,scroll[0] | (scroll[1] << 8));
		tilemap_set_scrolly(bg_tilemap,0,scroll[2] | (scroll[3] << 8));
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
		/* Draw the sprites. */
		for (offs = spriteram_size-4; offs>=0;offs -= 4)
		{
			/* SPRITES
			=====
			Attribute
			0x80 Code MSB
			0x40 Code MSB
			0x20 Code MSB
			0x10 Colour
			0x08 Colour
			0x04 Colour
			0x02 y Flip
			0x01 X MSB
			*/
	
	
			int code,colour,sx,sy,flipy;
			int attr = buffered_spriteram[offs+1];
			code = buffered_spriteram[offs];
			code += ( (attr&0xe0) << 3 );
			colour = (attr & 0x1c)>>2;
			sy = buffered_spriteram[offs + 2];
			sx = buffered_spriteram[offs + 3] + 0x100 * ( attr & 0x01);
			flipy = attr & 0x02;
	
			if (flip_screen != 0)
			{
				sx = 496 - sx;
				sy = 240 - sy;
				flipy = !flipy;
			}
	
			drawgfx(bitmap,Machine.gfx[2],
					code,
					colour,
					flip_screen,flipy,
					sx, sy,
					&Machine.visible_area,TRANSPARENCY_PEN,15);
		}
	}
	
	
	public static VhUpdatePtr srumbler_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		if (palette_recalc())
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,bg_tilemap,TILEMAP_BACK);
		draw_sprites(bitmap);
		tilemap_draw(bitmap,bg_tilemap,TILEMAP_FRONT);
		tilemap_draw(bitmap,fg_tilemap,0);
	} };
	
	public static VhEofCallbackPtr srumbler_eof_callback = new VhEofCallbackPtr() { public void handler() 
	{
		buffer_spriteram_w(0,0);
	} };
}
