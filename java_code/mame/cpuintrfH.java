#ifndef CPUINTRF_H
#define CPUINTRF_H

/*
 * ported to v0.37b5
 * using automatic conversion tool v0.01
 */ 
package mame;

public class cpuintrfH
{
	
	#ifdef __cplusplus
	extern "C" {
	#endif
	
	/* The old system is obsolete and no longer supported by the core */
	#define NEW_INTERRUPT_SYSTEM    1
	
	#define MAX_IRQ_LINES   8       /* maximum number of IRQ lines per CPU */
	
	#define CLEAR_LINE		0		/* clear (a fired, held or pulsed) line */
	#define ASSERT_LINE     1       /* assert an interrupt immediately */
	#define HOLD_LINE       2       /* hold interrupt line until enable is true */
	#define PULSE_LINE		3		/* pulse interrupt line for one instruction */
	
	#define MAX_REGS		128 	/* maximum number of register of any CPU */
	
	/* Values passed to the cpu_info function of a core to retrieve information */
	enum {
		CPU_INFO_REG,
		CPU_INFO_FLAGS=MAX_REGS,
		CPU_INFO_NAME,
		CPU_INFO_FAMILY,
		CPU_INFO_VERSION,
		CPU_INFO_FILE,
		CPU_INFO_CREDITS,
		CPU_INFO_REG_LAYOUT,
		CPU_INFO_WIN_LAYOUT
	};
	
	#define CPU_IS_LE		0	/* emulated CPU is little endian */
	#define CPU_IS_BE		1	/* emulated CPU is big endian */
	
	/*
	 * This value is passed to cpu_get_reg to retrieve the previous
	 * program counter value, ie. before a CPU emulation started
	 * to fetch opcodes and arguments for the current instrution.
	 */
	#define REG_PREVIOUSPC	-1
	
	/*
	 * This value is passed to cpu_get_reg/cpu_set_reg, instead of one of
	 * the names from the enum a CPU core defines for it's registers,
	 * to get or set the contents of the memory pointed to by a stack pointer.
	 * You can specify the n'th element on the stack by (REG_SP_CONTENTS-n),
	 * ie. lower negative values. The actual element size (UINT16 or UINT32)
	 * depends on the CPU core.
	 * This is also used to replace the cpu_geturnpc() function.
	 */
	#define REG_SP_CONTENTS -2
	
	
	
	/* ASG 971222 -- added this generic structure */
	struct cpu_interface
	{
		unsigned cpu_num;
		void (*reset)(void *param);
		void (*exit)(void);
		int (*execute)(int cycles);
		void (*burn)(int cycles);
		unsigned (*get_context)(void *reg);
		void (*set_context)(void *reg);
		void *(*get_cycle_table)(int which);
		void (*set_cycle_table)(int which, void *new_table);
	    unsigned (*get_pc)(void);
		void (*set_pc)(unsigned val);
		unsigned (*get_sp)(void);
		void (*set_sp)(unsigned val);
		unsigned (*get_reg)(int regnum);
		void (*set_reg)(int regnum, unsigned val);
		void (*set_nmi_line)(int linestate);
		void (*set_irq_line)(int irqline, int linestate);
		void (*set_irq_callback)(int(*callback)(int irqline));
		void (*internal_interrupt)(int type);
		void (*cpu_state_save)(void *file);
		void (*cpu_state_load)(void *file);
		const char* (*cpu_info)(void *context,int regnum);
		unsigned (*cpu_dasm)(char *buffer,unsigned pc);
		unsigned num_irqs;
		int default_vector;
		int *icount;
		double overclock;
		int no_int, irq_int, nmi_int;
		mem_read_handler memory_read;
		mem_write_handler memory_write;
		mem_read_handler internal_read;
		mem_write_handler internal_write;
		unsigned pgm_memory_base;
		void (*set_op_base)(int pc);
		int address_shift;
		unsigned address_bits, endianess, align_unit, max_inst_len;
		unsigned abits1, abits2, abitsmin;
	};
	
	extern struct cpu_interface cpuintf[];
	
	
	/* optional watchdog */
	/* Use this function to reset the machine */
	/* Use this function to reset a single CPU */
	void cpu_set_reset_line(int cpu,int state);
	/* Use this function to halt a single CPU */
	void cpu_set_halt_line(int cpu,int state);
	
	/* This function returns CPUNUM current status (running or halted) */
	int cpu_getstatus(int cpunum);
	void cpu_setactivecpu(int cpunum);
	
	/* Returns the current program counter */
	unsigned cpu_get_pc(void);
	/* Set the current program counter */
	void cpu_set_pc(unsigned val);
	
	/* Returns the current stack pointer */
	unsigned cpu_get_sp(void);
	/* Set the current stack pointer */
	void cpu_set_sp(unsigned val);
	
	/* Get the active CPUs context and return it's size */
	unsigned cpu_get_context(void *context);
	/* Set the active CPUs context */
	void cpu_set_context(void *context);
	
	/* Get a pointer to the active CPUs cycle count lookup table */
	void *cpu_get_cycle_table(int which);
	/* Override a pointer to the active CPUs cycle count lookup table */
	void cpu_set_cycle_tbl(int which, void *new_table);
	
	/* Returns a specific register value (mamedbg) */
	unsigned cpu_get_reg(int regnum);
	/* Sets a specific register value (mamedbg) */
	void cpu_set_reg(int regnum, unsigned val);
	
	/* Returns previous pc (start of opcode causing read/write) */
	/* #define cpu_getpreviouspc() cpu_get_reg(REG_PREVIOUSPC)
	
	/* Returns the return address from the top of the stack (Z80 only) */
	/* /* This can now be handled with a generic function */
	#define cpu_geturnpc() cpu_get_reg(REG_SP_CONTENTS)
	
	void cpu_set_op_base(unsigned val);
	
	/* Returns the number of CPU cycles which take place in one video frame */
	/* Returns the number of CPU cycles before the next interrupt handler call */
	/* Returns the number of CPU cycles before the end of the current video frame */
	/* Returns the number of CPU cycles in one video frame */
	/* Scales a given value by the ratio of fcount / fperiod */
	int cpu_scalebyfcount(int value);
	/* Returns the current scanline number */
	/* Returns the amount of time until a given scanline */
	double cpu_getscanlinetime(int scanline);
	/* Returns the duration of a single scanline */
	double cpu_getscanlineperiod(void);
	/* Returns the duration of a single scanline in cycles */
	/* Returns the number of cycles since the beginning of this frame */
	/* Returns the current horizontal beam position in pixels */
	/*
	  Returns the number of times the interrupt handler will be called before
	  the end of the current video frame. This is can be useful to interrupt
	  handlers to synchronize their operation. If you call this from outside
	  an interrupt handler, add 1 to the result, i.e. if it returns 0, it means
	  that the interrupt handler will be called once.
	*/
	
	/* Returns the current VBLANK state */
	
	/* Returns the number of the video frame we are currently playing */
	
	
	/* generate a trigger after a specific period of time */
	void cpu_triggertime (double duration, int trigger);
	/* generate a trigger now */
	void cpu_trigger (int trigger);
	
	/* burn CPU cycles until a timer trigger */
	void cpu_spinuntil_trigger (int trigger);
	/* burn CPU cycles until the next interrupt */
	/* burn CPU cycles until our timeslice is up */
	/* burn CPU cycles for a specific period of time */
	void cpu_spinuntil_time (double duration);
	
	/* yield our timeslice for a specific period of time */
	void cpu_yielduntil_trigger (int trigger);
	/* yield our timeslice until the next interrupt */
	/* yield our current timeslice */
	/* yield our timeslice for a specific period of time */
	void cpu_yielduntil_time (double duration);
	
	/* set the NMI line state for a CPU, normally use PULSE_LINE */
	void cpu_set_nmi_line(int cpunum, int state);
	/* set the IRQ line state for a specific irq line of a CPU */
	/* normally use state HOLD_LINE, irqline 0 for first IRQ type of a cpu */
	void cpu_set_irq_line(int cpunum, int irqline, int state);
	/* this is to be called by CPU cores only! */
	void cpu_generate_internal_interrupt(int cpunum, int type);
	/* set the vector to be returned during a CPU's interrupt acknowledge cycle */
	void cpu_irq_line_vector_w(int cpunum, int irqline, int vector);
	
	/* use this function to install a driver callback for IRQ acknowledge */
	void cpu_set_irq_callback(int cpunum, int (*callback)(int));
	
	/* use these in your write memory/port handles to set an IRQ vector */
	/* offset corresponds to the irq line number here */
	
	/* Obsolete functions: avoid to use them in new drivers if possible. */
	
	/* cause an interrupt on a CPU */
	void cpu_cause_interrupt(int cpu,int type);
	void cpu_clear_pending_interrupts(int cpu);
	#if (HAS_M68000 || HAS_M68010 || HAS_M68020 || HAS_M68EC020)
	#endif
	
	/* CPU context access */
	void* cpu_getcontext (int _activecpu);
	int cpu_is_saving_context(int _activecpu);
	
	/***************************************************************************
	 * Get information for the currently active CPU
	 * cputype is a value from the CPU enum in driver.h
	 ***************************************************************************/
	/* Return number of address bits */
	unsigned cpu_address_bits(void);
	/* Return address mask */
	unsigned cpu_address_mask(void);
	/* Return address shift factor (TMS34010 bit addressing mode) */
	/* Return endianess of the emulated CPU (CPU_IS_LE or CPU_IS_BE) */
	unsigned cpu_endianess(void);
	/* Return opcode align unit (1 byte, 2 word, 4 dword) */
	unsigned cpu_align_unit(void);
	/* Return maximum instruction length */
	unsigned cpu_max_inst_len(void);
	
	/* Return name of the active CPU */
	const char *cpu_name(void);
	/* Return family name of the active CPU */
	const char *cpu_core_family(void);
	/* Return core version of the active CPU */
	const char *cpu_core_version(void);
	/* Return core filename of the active CPU */
	const char *cpu_core_file(void);
	/* Return credits info for of the active CPU */
	const char *cpu_core_credits(void);
	/* Return register layout definition for the active CPU */
	const char *cpu_reg_layout(void);
	/* Return (debugger) window layout definition for the active CPU */
	const char *cpu_win_layout(void);
	
	/* Disassemble an instruction at PC into the given buffer */
	unsigned cpu_dasm(char *buffer, unsigned pc);
	/* Return a string describing the currently set flag (status) bits of the active CPU */
	const char *cpu_flags(void);
	/* Return a string with a register name and hex value for the active CPU */
	/* regnum is a value defined in the CPU cores header files */
	const char *cpu_dump_reg(int regnum);
	/* Return a string describing the active CPUs current state */
	const char *cpu_dump_state(void);
	
	/***************************************************************************
	 * Get information for a specific CPU type
	 * cputype is a value from the CPU enum in driver.h
	 ***************************************************************************/
	/* Return address shift factor */
	/* TMS320C10 -1: word addressing mode, TMS34010 3: bit addressing mode */
	int cputype_address_shift(int cputype);
	/* Return number of address bits */
	unsigned cputype_address_bits(int cputype);
	/* Return address mask */
	unsigned cputype_address_mask(int cputype);
	/* Return endianess of the emulated CPU (CPU_IS_LE or CPU_IS_BE) */
	unsigned cputype_endianess(int cputype);
	/* Return opcode align unit (1 byte, 2 word, 4 dword) */
	unsigned cputype_align_unit(int cputype);
	/* Return maximum instruction length */
	unsigned cputype_max_inst_len(int cputype);
	
	/* Return name of the CPU */
	const char *cputype_name(int cputype);
	/* Return family name of the CPU */
	const char *cputype_core_family(int cputype);
	/* Return core version number of the CPU */
	const char *cputype_core_version(int cputype);
	/* Return core filename of the CPU */
	const char *cputype_core_file(int cputype);
	/* Return credits for the CPU core */
	const char *cputype_core_credits(int cputype);
	/* Return register layout definition for the CPU core */
	const char *cputype_reg_layout(int cputype);
	/* Return (debugger) window layout definition for the CPU core */
	const char *cputype_win_layout(int cputype);
	
	/***************************************************************************
	 * Get (or set) information for a numbered CPU of the running machine
	 * cpunum is a value between 0 and cpu_gettotalcpu() - 1
	 ***************************************************************************/
	/* Return number of address bits */
	unsigned cpunum_address_bits(int cputype);
	/* Return address mask */
	unsigned cpunum_address_mask(int cputype);
	/* Return endianess of the emulated CPU (CPU_LSB_FIRST or CPU_MSB_FIRST) */
	unsigned cpunum_endianess(int cputype);
	/* Return opcode align unit (1 byte, 2 word, 4 dword) */
	unsigned cpunum_align_unit(int cputype);
	/* Return maximum instruction length */
	unsigned cpunum_max_inst_len(int cputype);
	
	/* Get a register value for the specified CPU number of the running machine */
	unsigned cpunum_get_reg(int cpunum, int regnum);
	/* Set a register value for the specified CPU number of the running machine */
	void cpunum_set_reg(int cpunum, int regnum, unsigned val);
	
	/* Return (debugger) register layout definition for the CPU core */
	const char *cpunum_reg_layout(int cpunum);
	/* Return (debugger) window layout definition for the CPU core */
	const char *cpunum_win_layout(int cpunum);
	
	unsigned cpunum_dasm(int cpunum,char *buffer,unsigned pc);
	/* Return a string describing the currently set flag (status) bits of the CPU */
	const char *cpunum_flags(int cpunum);
	/* Return a string with a register name and value */
	/* regnum is a value defined in the CPU cores header files */
	const char *cpunum_dump_reg(int cpunum, int regnum);
	/* Return a string describing the CPUs current state */
	const char *cpunum_dump_state(int cpunum);
	/* Return a name for the specified cpu number */
	const char *cpunum_name(int cpunum);
	/* Return a family name for the specified cpu number */
	const char *cpunum_core_family(int cpunum);
	/* Return a version for the specified cpu number */
	const char *cpunum_core_version(int cpunum);
	/* Return a the source filename for the specified cpu number */
	const char *cpunum_core_file(int cpunum);
	/* Return a the credits for the specified cpu number */
	const char *cpunum_core_credits(int cpunum);
	
	/* Dump all of the running machines CPUs state to stderr */
	
	/* daisy-chain link */
	typedef struct {
		void (*reset)(int);             /* reset callback     */
		int  (*interrupt_entry)(int);   /* entry callback     */
		void (*interrupt_reti)(int);    /* reti callback      */
		int irq_param;                  /* callback paramater */
	}	Z80_DaisyChain;
	
	#define Z80_MAXDAISY	4		/* maximum of daisy chan device */
	
	#define Z80_INT_REQ     0x01    /* interrupt request mask       */
	#define Z80_INT_IEO     0x02    /* interrupt disable mask(IEO)  */
	
	#define Z80_VECTOR(device,state) (((device)<<8)|(state))
	
	#ifdef __cplusplus
	}
	#endif
	
	#endif	/* CPUINTRF_H */
}
