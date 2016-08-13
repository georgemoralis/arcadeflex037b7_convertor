/***************************************************************************

Atari Avalanche machine

If you have any questions about how this driver works, don't hesitate to
ask.  - Mike Balfour (mab22@po.cwru.edu)
***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

static int attract = 0;		/* Turns off sound in attract mode */
static int volume = 0;		/* Volume of the noise signal */
static int aud0 = 0;		/* Enable/disable noise */
static int aud1 = 0;		/* Enable/disable 32V tone */
static int aud2 = 0;		/* Enable/disable 8V tone */
static int noise_k4=0;		/* First noise register */
static int noise_l4=0;		/* Second noise register */
static int noise=0;			/* Output noise signal */

/***************************************************************************
  avalnche_input_r
***************************************************************************/

READ_HANDLER( avalnche_input_r )
{
	switch (offset & 0x03)
	{
		case 0x00:	 return input_port_0_r(offset);
		case 0x01:	 return input_port_1_r(offset);
		case 0x02:	 return input_port_2_r(offset);
		case 0x03:	 return 0; /* Spare */
	}
	return 0;
}

/***************************************************************************
  avalnche_output_w
***************************************************************************/

WRITE_HANDLER( avalnche_output_w )
{
	switch (offset & 0x07)
	{
		case 0x00:		/* 1 CREDIT LAMP */
	        set_led_status(0,data & 0x01);
			break;
		case 0x01:		/* ATTRACT */
			attract = data & 0x01;
			break;
		case 0x02:		/* VIDEO INVERT */
			if (data & 0x01)
			{
				palette_change_color(0,0,0,0);
				palette_change_color(1,255,255,255);
			}
			else
			{
				palette_change_color(0,255,255,255);
				palette_change_color(1,0,0,0);
			}
			break;
		case 0x03:		/* 2 CREDIT LAMP */
	        set_led_status(1,data & 0x01);
			break;
		case 0x04:		/* AUD0 */
			aud0 = data & 0x01;
			break;
		case 0x05:		/* AUD1 */
			aud1 = data & 0x01;
			break;
		case 0x06:		/* AUD2 */
			aud2 = data & 0x01;
			break;
		case 0x07:		/* START LAMP (Serve button) */
	        set_led_status(2,data & 0x01);
			break;
	}
}

/***************************************************************************
  avalnche_noise_amplitude_w
***************************************************************************/

WRITE_HANDLER( avalnche_noise_amplitude_w )
{
	volume = data & 0x3F;
}

/***************************************************************************
  avalnche_interrupt

  Since all of the sound for this game is driven off of a vertical timer
  signal, and our interrupts are driven off of a vertical timer signal,
  we just handle generation of all of the sound signals inside of the
  interrupt.  Note that the interrupt must occur twice as fast as the
  fastest timer needed so that we can turn off sound on the "off" time.
***************************************************************************/

int avalnche_interrupt(void)
{
	static int time_8V  = 0;
	static int time_16V = 0;
	static int time_32V = 0;
	int k4_input;
	int l4_input;

	time_8V  = (time_8V  + 1) & 0x01;
	time_16V = (time_16V + 1) & 0x03;
	time_32V = (time_32V + 1) & 0x07;

	/* One audio tone is generated from an 8V clock */
	if ((attract==0) && (aud2==1) && (time_8V==0))
		DAC_data_w(0,255);
	/* One audio tone is generated from a 32V clock */
	else if ((attract==0) && (aud1==1) && (time_32V==0))
		DAC_data_w(0,255);
	else
		DAC_data_w(0,0);

	/* Noise is driven by a 16V clock I think */
	if ((attract==0) && (time_16V==0))
	{
		l4_input = 0x01 ^ ((noise_l4 & 0x01) ^ (((noise_k4) & 0x40) >> 6));
		k4_input = (noise_l4 & 0x80) >> 7;

		noise_l4 = ((noise_l4 << 1) | l4_input) & 0xFF;
		noise_k4 = ((noise_k4 << 1) | k4_input) & 0xFF;

		noise = (noise_k4 & 0x80) >> 7;
	}


	if ((aud0==0) && (noise))
		DAC_data_w(1,volume);
	else
		DAC_data_w(1,0);

	/* Interrupt is generated by a 16V clock */
	if (time_16V==0)
		return nmi_interrupt();
	else
		return 0;
}

