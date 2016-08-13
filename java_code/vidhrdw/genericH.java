/*
 * ported to v0.37b5
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class genericH
{
	
	#ifdef __cplusplus
	extern "C" {
	#endif
	
	extern unsigned char *videoram;
	extern size_t videoram_size;
	extern unsigned char *colorram;
	extern unsigned char *spriteram;
	extern size_t spriteram_size;
	extern unsigned char *spriteram_2;
	extern size_t spriteram_2_size;
	extern unsigned char *spriteram_3;
	extern size_t spriteram_3_size;
	extern unsigned char *buffered_spriteram;
	extern unsigned char *buffered_spriteram_2;
	extern unsigned char *dirtybuffer;
	extern struct osd_bitmap *tmpbitmap;
	
	void buffer_spriteram(unsigned char *ptr,int length);
	void buffer_spriteram_2(unsigned char *ptr,int length);
	
	#ifdef __cplusplus
	}
	#endif
}
