package MOTO6809;

public class CPU6809 {
	private final Registres registres;
	private final Memoire mem;
	private boolean halted = false;
	private boolean waitingForInterrupt = false;
	private long cycleCount = 0;
	private String currentInstruction = "";

	public CPU6809(Registres registers, Memoire mem) {
		this.registres = registers;
		this.mem = mem;
	}

	// ============================================
	// CLASSE POUR RÉSULTATS D'ADRESSAGE
	// ============================================

	private static class AddressingResult {
		int value;
		int address;
		String description;
		int cycles;
		boolean is16bit;

		AddressingResult(int val, int addr, String desc, int cyc, boolean is16) {
			this.value = val;
			this.address = addr;
			this.description = desc;
			this.cycles = cyc;
			this.is16bit = is16;
		}

		AddressingResult(int val, int addr, String desc, int cyc) {
			this(val, addr, desc, cyc, false);
		}
	}

	// ============================================
	// RESET ET CONTRÔLE DU CPU
	// ============================================

	public void reset() {
		registres.reset();
		int resetVector = mem.getWord(0xFFFE);

		if (resetVector == 0x0000 || resetVector == 0xFFFF) {
			System.err.println("\n⚠️  AVERTISSEMENT: Vecteur de reset invalide!");
			System.err.printf("   Vecteur lu à $FFFE-$FFFF: $%04X\n", resetVector);
		}

		registres.setPC(resetVector);
		halted = false;
		cycleCount = 0;
		currentInstruction = "";

		System.out.println("========================================");
		System.out.println("         CPU RESET");
		System.out.println("========================================");
		System.out.printf("Vecteur de reset: $%04X\n", resetVector);
		System.out.printf("PC initialisé à : $%04X\n", registres.getPC());
		System.out.printf("S (Stack): $%04X\n", registres.getS());
		System.out.printf("CC: $%02X (I=%s, F=%s)\n", registres.getCC(), registres.isIRQMasked() ? "1" : "0",
				registres.isFIRQMasked() ? "1" : "0");
		System.out.println("========================================\n");
	}

	public void halt() {
		halted = true;
		System.out.println("\n⛔ CPU HALTED!\n");
	}

	public boolean isHalted() {
		return halted;
	}

	public long getCycleCount() {
		return cycleCount;
	}

	public Registres getRegisters() {
		return registres;
	}

	public Memoire getMemory() {
		return mem;
	}

	public String getCurrentInstruction() {
		return currentInstruction;
	}

	// ============================================
	// DÉCODEUR UNIVERSEL DES MODES D'ADRESSAGE
	// ============================================

	private AddressingResult decodeAddressing(int opcode, boolean is16bit) {
		int mode = (opcode >> 4) & 0x0F;

		switch (mode) {
		case 0x08:
		case 0x0C:
			return decodeImmediate(is16bit);
		case 0x09:
		case 0x0D:
			return decodeDirect(is16bit);
		case 0x0A:
		case 0x0E:
			return decodeIndexed(is16bit);
		case 0x0B:
		case 0x0F:
			return decodeExtended(is16bit);
		default:
			return new AddressingResult(0, 0, "???", 0, is16bit);
		}
	}

	private AddressingResult decodeImmediate(boolean is16bit) {
		if (is16bit) {
			int value = fetchWord();
			return new AddressingResult(value, -1, String.format("#$%04X", value), 3, true);
		} else {
			int value = fetchByte();
			return new AddressingResult(value, -1, String.format("#$%02X", value), 2, false);
		}
	}

	private AddressingResult decodeDirect(boolean is16bit) {
		int offset = fetchByte();
		int address = (registres.getDP() << 8) | offset;
		String desc = String.format("$%02X", offset);

		if (is16bit) {
			return new AddressingResult(mem.getWord(address), address, desc, 5, true);
		} else {
			return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 4, false);
		}
	}

	private AddressingResult decodeExtended(boolean is16bit) {
		int address = fetchWord();
		String desc = String.format("$%04X", address);

		if (is16bit) {
			return new AddressingResult(mem.getWord(address), address, desc, 6, true);
		} else {
			return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 5, false);
		}
	}

	private AddressingResult decodeIndexed(boolean is16bit) {
		int postbyte = fetchByte();
		int regBits = (postbyte >> 5) & 0x03;
		int baseReg = getIndexRegister(regBits);
		String regName = getIndexRegisterName(regBits);

		// Offset 5-bit
		if ((postbyte & 0x80) == 0) {
			int offset = postbyte & 0x1F;
			if ((offset & 0x10) != 0)
				offset |= 0xFFFFFFE0;
			int address = (baseReg + offset) & 0xFFFF;
			String desc = String.format("%d,%s", offset, regName);

			if (is16bit) {
				return new AddressingResult(mem.getWord(address), address, desc, 5, true);
			} else {
				return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 4, false);
			}
		}

		// Modes étendus
		int mode = postbyte & 0x0F;
		switch (mode) {
		case 0x00: { // ,R+
			int address = baseReg;
			setIndexRegister(regBits, baseReg + 1);
			String desc = String.format(",%s+", regName);
			if (is16bit)
				return new AddressingResult(mem.getWord(address), address, desc, 6, true);
			else
				return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 5, false);
		}
		case 0x01: { // ,R++
			int address = baseReg;
			setIndexRegister(regBits, baseReg + 2);
			String desc = String.format(",%s++", regName);
			if (is16bit)
				return new AddressingResult(mem.getWord(address), address, desc, 7, true);
			else
				return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 6, false);
		}
		case 0x02: { // ,-R
			int address = (baseReg - 1) & 0xFFFF;
			setIndexRegister(regBits, address);
			String desc = String.format(",-%s", regName);
			if (is16bit)
				return new AddressingResult(mem.getWord(address), address, desc, 6, true);
			else
				return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 5, false);
		}
		case 0x03: { // ,--R
			int address = (baseReg - 2) & 0xFFFF;
			setIndexRegister(regBits, address);
			String desc = String.format(",--%s", regName);
			if (is16bit)
				return new AddressingResult(mem.getWord(address), address, desc, 7, true);
			else
				return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 6, false);
		}
		case 0x04: { // ,R
			int address = baseReg;
			String desc = String.format(",%s", regName);
			if (is16bit)
				return new AddressingResult(mem.getWord(address), address, desc, 5, true);
			else
				return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 4, false);
		}
		case 0x05: { // B,R
			int offset = registres.getB();
			if ((offset & 0x80) != 0)
				offset |= 0xFFFFFF00;
			int address = (baseReg + offset) & 0xFFFF;
			String desc = String.format("B,%s", regName);
			if (is16bit)
				return new AddressingResult(mem.getWord(address), address, desc, 6, true);
			else
				return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 5, false);
		}
		case 0x06: { // A,R
			int offset = registres.getA();
			if ((offset & 0x80) != 0)
				offset |= 0xFFFFFF00;
			int address = (baseReg + offset) & 0xFFFF;
			String desc = String.format("A,%s", regName);
			if (is16bit)
				return new AddressingResult(mem.getWord(address), address, desc, 6, true);
			else
				return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 5, false);
		}
		case 0x08: { // Offset 8-bit
			int offset = fetchByte();
			if ((offset & 0x80) != 0)
				offset |= 0xFFFFFF00;
			int address = (baseReg + offset) & 0xFFFF;
			String desc = String.format("$%02X,%s", offset & 0xFF, regName);
			if (is16bit)
				return new AddressingResult(mem.getWord(address), address, desc, 6, true);
			else
				return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 5, false);
		}
		case 0x09: { // Offset 16-bit
			int offset = fetchWord();
			int address = (baseReg + offset) & 0xFFFF;
			String desc = String.format("$%04X,%s", offset, regName);
			if (is16bit)
				return new AddressingResult(mem.getWord(address), address, desc, 9, true);
			else
				return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 8, false);
		}
		case 0x0B: { // D,R
			int offset = registres.getD();
			if ((offset & 0x8000) != 0)
				offset |= 0xFFFF0000;
			int address = (baseReg + offset) & 0xFFFF;
			String desc = String.format("D,%s", regName);
			if (is16bit)
				return new AddressingResult(mem.getWord(address), address, desc, 9, true);
			else
				return new AddressingResult(mem.getByte(address) & 0xFF, address, desc, 8, false);
		}
		default:
			return new AddressingResult(0, baseReg, "???", 4, is16bit);
		}
	}

	// ============================================
	// HELPERS POUR REGISTRES D'INDEX
	// ============================================

	private int getIndexRegister(int regBits) {
		switch (regBits) {
		case 0:
			return registres.getX();
		case 1:
			return registres.getY();
		case 2:
			return registres.getU();
		case 3:
			return registres.getS();
		default:
			return 0;
		}
	}

	// ============================================
	// DÉCODAGE SPÉCIAL POUR ASR, ASL, COM, LSR, etc.
	// ============================================
	private AddressingResult decodeShiftRotate(int opcode) {
		// Détection du mode d'adressage basé sur le nibble haut
		int upperNibble = (opcode >> 4) & 0x0F;

		// Mode DIRECT: $0x ($03, $07, $08, etc.)
		if (upperNibble == 0x00) {
			int offset = fetchByte();
			int address = (registres.getDP() << 8) | offset;
			int value = mem.getByte(address) & 0xFF;
			return new AddressingResult(value, address, String.format("$%02X", offset), 6, false);
		}

		// Mode INDEXED: $6x ($63, $67, $68, etc.)
		if (upperNibble == 0x06) {
			int postbyte = fetchByte();
			int regBits = (postbyte >> 5) & 0x03;
			int baseReg = getIndexRegister(regBits);
			String regName = getIndexRegisterName(regBits);

			// Offset 5-bit
			if ((postbyte & 0x80) == 0) {
				int offset = postbyte & 0x1F;
				if ((offset & 0x10) != 0)
					offset |= 0xFFFFFFE0;
				int address = (baseReg + offset) & 0xFFFF;
				int value = mem.getByte(address) & 0xFF;
				return new AddressingResult(value, address, String.format("%d,%s", offset, regName), 6, false);
			}

			// Mode ,R (postbyte = 0x84)
			if ((postbyte & 0x9F) == 0x84) {
				int address = baseReg;
				int value = mem.getByte(address) & 0xFF;
				String desc = String.format(",%s", regName);
				return new AddressingResult(value, address, desc, 6, false);
			}

			// Mode offset 8-bit
			if ((postbyte & 0x9F) == 0x88) {
				int offset = fetchByte();
				if ((offset & 0x80) != 0)
					offset |= 0xFFFFFF00;
				int address = (baseReg + offset) & 0xFFFF;
				int value = mem.getByte(address) & 0xFF;
				String desc = String.format("$%02X,%s", offset & 0xFF, regName);
				return new AddressingResult(value, address, desc, 6, false);
			}
		}

		// Mode EXTENDED: $7x ($73, $77, $78, etc.)
		if (upperNibble == 0x07) {
			int address = fetchWord();
			int value = mem.getByte(address) & 0xFF;
			return new AddressingResult(value, address, String.format("$%04X", address), 7, false);
		}

		return new AddressingResult(0, 0, "???", 0, false);
	}

	private String getIndexRegisterName(int regBits) {
		switch (regBits) {
		case 0:
			return "X";
		case 1:
			return "Y";
		case 2:
			return "U";
		case 3:
			return "S";
		default:
			return "?";
		}
	}

	private void setIndexRegister(int regBits, int value) {
		value &= 0xFFFF;
		switch (regBits) {
		case 0:
			registres.setX(value);
			break;
		case 1:
			registres.setY(value);
			break;
		case 2:
			registres.setU(value);
			break;
		case 3:
			registres.setS(value);
			break;
		}
	}

	// ============================================
	// GESTION DES FLAGS
	// ============================================

	private void updateNZFlags(int value) {
		registres.setFlag(Registres.CC_N, (value & 0x80) != 0);
		registres.setFlag(Registres.CC_Z, (value & 0xFF) == 0);
	}

	private void updateNZFlags16(int value) {
		registres.setFlag(Registres.CC_N, (value & 0x8000) != 0);
		registres.setFlag(Registres.CC_Z, (value & 0xFFFF) == 0);
	}

	private void updateNZVCFlagsAdd8(int op1, int op2, int result) {
		registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
		registres.setFlag(Registres.CC_Z, (result & 0xFF) == 0);
		registres.setFlag(Registres.CC_C, (result & 0x100) != 0);
		registres.setFlag(Registres.CC_V, ((op1 & 0x80) == (op2 & 0x80)) && ((op1 & 0x80) != (result & 0x80)));
	}

	private void updateNZVCFlagsSub8(int op1, int op2, int result) {
		registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
		registres.setFlag(Registres.CC_Z, (result & 0xFF) == 0);
		registres.setFlag(Registres.CC_C, (result & 0x100) != 0);
		registres.setFlag(Registres.CC_V, ((op1 & 0x80) != (op2 & 0x80)) && ((op1 & 0x80) != (result & 0x80)));
	}

	private void updateNZVCFlagsAdd16(int op1, int op2, int result) {
		// N: bit 15 du résultat
		registres.setFlag(Registres.CC_N, (result & 0x8000) != 0);

		// Z: résultat est zéro
		registres.setFlag(Registres.CC_Z, (result & 0xFFFF) == 0);

		// C: carry du bit 15 (débordement 16-bit)
		registres.setFlag(Registres.CC_C, (result & 0x10000) != 0);

		// V: overflow (débordement signé)
		// Overflow si: les deux opérandes ont le même signe ET
		// le résultat a un signe différent
		registres.setFlag(Registres.CC_V, ((op1 & 0x8000) == (op2 & 0x8000)) && ((op1 & 0x8000) != (result & 0x8000)));
	}

	private void updateNZVCFlagsSub16(int op1, int op2, int result) {
		// N: bit 15 du résultat
		registres.setFlag(Registres.CC_N, (result & 0x8000) != 0);

		// Z: résultat est zéro
		registres.setFlag(Registres.CC_Z, (result & 0xFFFF) == 0);

		// C: borrow (pas de carry en soustraction)
		registres.setFlag(Registres.CC_C, (result & 0x10000) != 0);

		// V: overflow signé en soustraction
		registres.setFlag(Registres.CC_V, ((op1 & 0x8000) != (op2 & 0x8000)) && ((op1 & 0x8000) != (result & 0x8000)));
	}
	// ============================================
	// GESTION DE LA PILE
	// ============================================

	private void pushStack8(int value) {
		int s = (registres.getS() - 1) & 0xFFFF;
		mem.setByte(s, (byte) (value & 0xFF));
		registres.setS(s);
	}

	private void pushStack16(int value) {
		pushStack8((value >> 8) & 0xFF);
		pushStack8(value & 0xFF);
	}

	private int pullStack8() {
		int s = registres.getS();
		int value = mem.getByte(s) & 0xFF;
		registres.setS((s + 1) & 0xFFFF);
		return value;
	}

	private int pullStack16() {
		int lo = pullStack8();
		int hi = pullStack8();
		return (hi << 8) | lo;
	}

	private void pushStackU8(int value) {
		int u = (registres.getU() - 1) & 0xFFFF;
		mem.setByte(u, (byte) (value & 0xFF));
		registres.setU(u);
	}

	private void pushStackU16(int value) {
		pushStackU8((value >> 8) & 0xFF); // High byte
		pushStackU8(value & 0xFF); // Low byte
	}

	private int pullStackU8() {
		int u = registres.getU();
		int value = mem.getByte(u) & 0xFF;
		registres.setU((u + 1) & 0xFFFF);
		return value;
	}

	private int pullStackU16() {
		int lo = pullStackU8();
		int hi = pullStackU8();
		return (hi << 8) | lo;
	}

	// ============================================
	// FETCH DES OPÉRANDES
	// ============================================

	public int fetchByte() {
		int value = mem.getByte(registres.getPC()) & 0xFF;
		registres.incPC(1);
		return value;
	}

	public int fetchWord() {
		return (fetchByte() << 8) | fetchByte();
	}

	// Compte le nombre de bits à 1 dans le masque
	private int countBitsSet(int mask) {
		int count = 0;
		for (int i = 0; i < 8; i++) {
			if ((mask & (1 << i)) != 0)
				count++;
		}
		return count;
	}

	// Formate le masque de registres pour l'affichage

	private String formatRegisterMask(int mask) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;

		if ((mask & 0x01) != 0) {
			sb.append("CC");
			first = false;
		}
		if ((mask & 0x02) != 0) {
			if (!first)
				sb.append(",");
			sb.append("A");
			first = false;
		}
		if ((mask & 0x04) != 0) {
			if (!first)
				sb.append(",");
			sb.append("B");
			first = false;
		}
		if ((mask & 0x08) != 0) {
			if (!first)
				sb.append(",");
			sb.append("DP");
			first = false;
		}
		if ((mask & 0x10) != 0) {
			if (!first)
				sb.append(",");
			sb.append("X");
			first = false;
		}
		if ((mask & 0x20) != 0) {
			if (!first)
				sb.append(",");
			sb.append("Y");
			first = false;
		}
		if ((mask & 0x40) != 0) {
			if (!first)
				sb.append(",");
			sb.append("U/S");
			first = false;
		}
		if ((mask & 0x80) != 0) {
			if (!first)
				sb.append(",");
			sb.append("PC");
		}

		return sb.toString();
	}

	// ============================================
	// EXÉCUTION DES INSTRUCTIONS
	// ============================================

	public void executeOneInstruction() {
		if (halted) {
			System.out.println("⚠️  CPU is halted! Cannot execute.");
			return;
		}

		// VÉRIFICATION
		if (waitingForInterrupt) {
			System.out.println("⏸️  CPU en attente d'interruption (CWAI)...");
			return; // Ne rien exécuter tant qu'il n'y a pas d'interruption
		}

		int pc = registres.getPC();
		if (pc == 0x0000 || pc == 0xFFFF) {
			System.err.printf("\n⛔ ERREUR FATALE: PC=$%04X est invalide!\n", pc);
			halt();
			return;
		}

		int opcode = mem.getByte(pc) & 0xFF;
		System.out.println("\n╔════════════════════════════════════════╗");
		System.out.printf("║ PC: $%04X  │  Opcode: $%02X              ║\n", pc, opcode);
		registres.incPC(1);

		// ============================================
		// GESTION DES INSTRUCTIONS PAGE 3 ($11 prefix)
		// ============================================
		if (opcode == 0x11) {
			int secondByte = fetchByte();
			System.out.printf("║ Page 3: Second byte = $%02X              ║\n", secondByte);

			String mnemonique = "";
			String operande = "";
			int cycles = 2;

			switch (secondByte) {
			case 0x83:
			case 0x93:
			case 0xA3:
			case 0xB3: {
				mnemonique = "CMPU";
				AddressingResult addr = decodeAddressing(0x80 | ((secondByte >> 4) & 0x0F) << 4, true);
				operande = " " + addr.description;

				// CMPU: Compare U avec la valeur
				int result = registres.getU() - addr.value;
				updateNZVCFlagsSub16(registres.getU(), addr.value, result);

				cycles = addr.cycles + 1;
				break;
			}

			case 0x8C:
			case 0x9C:
			case 0xAC:
			case 0xBC: {
				mnemonique = "CMPS";
				AddressingResult addr = decodeAddressing(0x80 | ((secondByte >> 4) & 0x0F) << 4, true);
				operande = " " + addr.description;

				// CMPS: Compare S avec la valeur
				int result = registres.getS() - addr.value;
				updateNZVCFlagsSub16(registres.getS(), addr.value, result);

				cycles = addr.cycles + 1;
				break;
			}
			// ========================================
			// SWI3 - Software Interrupt 3 (Page 3)
			// ========================================

			case 0x3F: {
				mnemonique = "SWI3";

				// SWI3 empile tout l'état (comme SWI)
				pushStack16(registres.getPC());
				pushStack16(registres.getU());
				pushStack16(registres.getY());
				pushStack16(registres.getX());
				pushStack8(registres.getDP());
				pushStack8(registres.getB());
				pushStack8(registres.getA());
				pushStack8(registres.getCC() | 0x80); // Set E flag

				int swi3Vector = mem.getWord(0xFFF2);
				registres.setPC(swi3Vector);

				cycles = 20;
				break;
			}

			default:
				mnemonique = "??? (Page 3)";
				operande = String.format(" $11 $%02X", secondByte);
				System.out.printf("║ ⚠️  OPCODE PAGE 3 NON IMPLÉMENTÉ: $11 $%02X  ║\n", secondByte);
				cycles = 1;
				break;
			}

			currentInstruction = mnemonique + operande;
			cycleCount += cycles;
			printDebugInfo(mnemonique, operande, cycles);
			return;
		}

		// ============================================
		// GESTION DES INSTRUCTIONS PAGE 2 ($10 prefix)
		// ============================================
		if (opcode == 0x10) {
			int secondByte = fetchByte();
			System.out.printf("║ Page 2: Second byte = $%02X              ║\n", secondByte);

			String mnemonique = "";
			String operande = "";
			int cycles = 2;

			switch (secondByte) {
			case 0x8E:
			case 0x9E:
			case 0xAE:
			case 0xBE: {
				mnemonique = "LDY";
				AddressingResult addr = decodeAddressing(0x80 | ((secondByte >> 4) & 0x0F) << 4, true);
				operande = " " + addr.description;
				registres.setY(addr.value);
				updateNZFlags16(registres.getY());
				cycles = addr.cycles + 1;
				break;
			}
			case 0xCE:
			case 0xDE:
			case 0xEE:
			case 0xFE: {
				mnemonique = "LDS";
				AddressingResult addr = decodeAddressing(0xC0 | ((secondByte >> 4) & 0x0F) << 4, true);
				operande = " " + addr.description;
				registres.setS(addr.value);
				updateNZFlags16(registres.getS());
				cycles = addr.cycles + 1;
				break;
			}
			case 0x9F:
			case 0xAF:
			case 0xBF: {
				mnemonique = "STY";
				AddressingResult addr = decodeAddressing(0x90 | ((secondByte >> 4) & 0x0F) << 4, true);
				operande = " " + addr.description;
				mem.setByte(addr.address, (byte) ((registres.getY() >> 8) & 0xFF));
				mem.setByte(addr.address + 1, (byte) (registres.getY() & 0xFF));
				updateNZFlags16(registres.getY());
				cycles = addr.cycles + 1;
				break;
			}
			case 0xDF:
			case 0xEF:
			case 0xFF: {
				mnemonique = "STS";
				AddressingResult addr = decodeAddressing(0xD0 | ((secondByte >> 4) & 0x0F) << 4, true);
				operande = " " + addr.description;
				mem.setByte(addr.address, (byte) ((registres.getS() >> 8) & 0xFF));
				mem.setByte(addr.address + 1, (byte) (registres.getS() & 0xFF));
				updateNZFlags16(registres.getS());
				cycles = addr.cycles + 1;
				break;
			}
			case 0x83:
			case 0x93:
			case 0xA3:
			case 0xB3: {
				mnemonique = "CMPD";
				AddressingResult addr = decodeAddressing(0x80 | ((secondByte >> 4) & 0x0F) << 4, true);
				operande = " " + addr.description;

				// CMPD: Compare D avec la valeur
				int result = registres.getD() - addr.value;
				updateNZVCFlagsSub16(registres.getD(), addr.value, result);

				cycles = addr.cycles + 1;
				break;
			}

			case 0x8C:
			case 0x9C:
			case 0xAC:
			case 0xBC: {
				mnemonique = "CMPY";
				AddressingResult addr = decodeAddressing(0x80 | ((secondByte >> 4) & 0x0F) << 4, true);
				operande = " " + addr.description;

				// CMPY: Compare Y avec la valeur
				int result = registres.getY() - addr.value;
				updateNZVCFlagsSub16(registres.getY(), addr.value, result);

				cycles = addr.cycles + 1;
				break;
			}
			// ========================================
			// SWI2 - Software Interrupt 2
			// ========================================
			case 0x3F: {
				mnemonique = "SWI2";

				// SWI2 empile tout l'état (comme SWI)
				pushStack16(registres.getPC());
				pushStack16(registres.getU());
				pushStack16(registres.getY());
				pushStack16(registres.getX());
				pushStack8(registres.getDP());
				pushStack8(registres.getB());
				pushStack8(registres.getA());
				pushStack8(registres.getCC() | 0x80); // Set E flag

				// SWI2 ne modifie PAS les flags I et F (différence avec SWI)
				// Charger le vecteur SWI2 ($FFF4-$FFF5)
				int swi2Vector = mem.getWord(0xFFF4);
				registres.setPC(swi2Vector);

				cycles = 20;
				break;
			}
			default:
				mnemonique = "??? (Page 2)";
				operande = String.format(" $10 $%02X", secondByte);
				System.out.printf("║ ⚠️  OPCODE PAGE 2 NON IMPLÉMENTÉ: $10 $%02X  ║\n", secondByte);
				cycles = 1;
				break;
			}

			currentInstruction = mnemonique + operande;
			cycleCount += cycles;
			printDebugInfo(mnemonique, operande, cycles);
			return;
		}

		// ============================================
		// INSTRUCTIONS PAGE 1 (normales)
		// ============================================
		String mnemonique = "";
		String operande = "";
		int cycles = 2;

		switch (opcode) {
		case 0x86:
		case 0x96:
		case 0xA6:
		case 0xB6: {
			mnemonique = "LDA";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;
			registres.setA(addr.value);
			updateNZFlags(registres.getA());
			cycles = addr.cycles;
			break;
		}
		case 0xC6:
		case 0xD6:
		case 0xE6:
		case 0xF6: {
			mnemonique = "LDB";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;
			registres.setB(addr.value);
			updateNZFlags(registres.getB());
			cycles = addr.cycles;
			break;
		}
		case 0xCC:
		case 0xDC:
		case 0xEC:
		case 0xFC: {
			mnemonique = "LDD";
			AddressingResult addr = decodeAddressing(opcode, true);
			operande = " " + addr.description;
			registres.setD(addr.value);
			updateNZFlags16(registres.getD());
			cycles = addr.cycles;
			break;
		}
		case 0xCE:
		case 0xDE:
		case 0xEE:
		case 0xFE: {
			mnemonique = "LDU";
			AddressingResult addr = decodeAddressing(opcode, true);
			operande = " " + addr.description;
			registres.setU(addr.value);
			updateNZFlags16(registres.getU());
			cycles = addr.cycles;
			break;
		}
		case 0x8E:
		case 0x9E:
		case 0xAE:
		case 0xBE: {
			mnemonique = "LDX";
			AddressingResult addr = decodeAddressing(opcode, true);
			operande = " " + addr.description;
			registres.setX(addr.value);
			updateNZFlags16(registres.getX());
			cycles = addr.cycles;
			break;
		}
		case 0x97:
		case 0xA7:
		case 0xB7: {
			mnemonique = "STA";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;
			mem.setByte(addr.address, (byte) registres.getA());
			updateNZFlags(registres.getA());
			cycles = addr.cycles;
			break;
		}
		case 0xD7:
		case 0xE7:
		case 0xF7: {
			mnemonique = "STB";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;
			mem.setByte(addr.address, (byte) registres.getB());
			updateNZFlags(registres.getB());
			cycles = addr.cycles;
			break;
		}
		case 0xDD:
		case 0xED:
		case 0xFD: {
			mnemonique = "STD";
			AddressingResult addr = decodeAddressing(opcode, true);
			operande = " " + addr.description;
			mem.setByte(addr.address, (byte) ((registres.getD() >> 8) & 0xFF));
			mem.setByte(addr.address + 1, (byte) (registres.getD() & 0xFF));
			updateNZFlags16(registres.getD());
			cycles = addr.cycles;
			break;
		}

		case 0x9F:
		case 0xAF:
		case 0xBF: {
			mnemonique = "STX";
			AddressingResult addr = decodeAddressing(opcode, true);
			operande = " " + addr.description;
			mem.setByte(addr.address, (byte) ((registres.getX() >> 8) & 0xFF));
			mem.setByte(addr.address + 1, (byte) (registres.getX() & 0xFF));
			updateNZFlags16(registres.getX());
			cycles = addr.cycles;
			break;
		}

		case 0xDF:
		case 0xEF:
		case 0xFF: {
			mnemonique = "STU";
			AddressingResult addr = decodeAddressing(opcode, true);
			operande = " " + addr.description;
			mem.setByte(addr.address, (byte) ((registres.getU() >> 8) & 0xFF));
			mem.setByte(addr.address + 1, (byte) (registres.getU() & 0xFF));
			updateNZFlags16(registres.getU());
			cycles = addr.cycles;
			break;
		}
		case 0x3A: {
			mnemonique = "ABX";
			// ABX ajoute le registre B (non signé) à X
			int result = (registres.getX() + registres.getB()) & 0xFFFF;
			registres.setX(result);
			cycles = 3;
			break;
		}
		case 0x89:
		case 0x99:
		case 0xA9:
		case 0xB9: {
			mnemonique = "ADCA";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// ADCA: A = A + M + C
			int oldA = registres.getA();
			int carry = registres.getFlag(Registres.CC_C) ? 1 : 0;
			int sum = oldA + addr.value + carry;

			registres.setA(sum);

			// Mise à jour des flags
			updateNZVCFlagsAdd8(oldA, addr.value + carry, sum);

			cycles = addr.cycles;
			break;
		}
		case 0xC9:
		case 0xD9:
		case 0xE9:
		case 0xF9: {
			mnemonique = "ADCB";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// ADCB: B = B + M + C
			int oldB = registres.getB();
			int carry = registres.getFlag(Registres.CC_C) ? 1 : 0;
			int sum = oldB + addr.value + carry;

			registres.setB(sum);

			// Mise à jour des flags
			updateNZVCFlagsAdd8(oldB, addr.value + carry, sum);

			cycles = addr.cycles;
			break;
		}
		case 0x8B:
		case 0x9B:
		case 0xAB:
		case 0xBB: {
			mnemonique = "ADDA";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;
			int oldA = registres.getA();
			int sum = oldA + addr.value;
			registres.setA(sum);
			updateNZVCFlagsAdd8(oldA, addr.value, sum);
			cycles = addr.cycles;
			break;
		}
		case 0xCB:
		case 0xDB:
		case 0xEB:
		case 0xFB: {
			mnemonique = "ADDB";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;
			int oldB = registres.getB();
			int sum = oldB + addr.value;
			registres.setB(sum);
			updateNZVCFlagsAdd8(oldB, addr.value, sum);
			cycles = addr.cycles;
			break;
		}
		case 0xC3:
		case 0xD3:
		case 0xE3:
		case 0xF3: {
			mnemonique = "ADDD";
			AddressingResult addr = decodeAddressing(opcode, true);
			operande = " " + addr.description;

			// ADDD: Ajouter au registre D (16-bit)
			int oldD = registres.getD();
			int sum = oldD + addr.value;

			// Mettre à jour D avec le résultat (16-bit)
			registres.setD(sum & 0xFFFF);

			// Mettre à jour les flags
			updateNZVCFlagsAdd16(oldD, addr.value, sum);

			cycles = addr.cycles;
			break;
		}
		case 0x84:
		case 0x94:
		case 0xA4:
		case 0xB4: {
			mnemonique = "ANDA";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// AND logique : A = A & M
			int result = registres.getA() & addr.value;
			registres.setA(result);

			// Mise à jour des flags
			updateNZFlags(result);
			registres.setFlag(Registres.CC_V, false); // V toujours à 0

			cycles = addr.cycles;
			break;
		}
		case 0xC4:
		case 0xD4:
		case 0xE4:
		case 0xF4: {
			mnemonique = "ANDB";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// AND logique : B = B & M
			int result = registres.getB() & addr.value;
			registres.setB(result);

			// Mise à jour des flags
			updateNZFlags(result);
			registres.setFlag(Registres.CC_V, false); // V toujours à 0

			cycles = addr.cycles;
			break;
		}
		case 0x1C: {
			mnemonique = "ANDCC";
			int mask = fetchByte();
			operande = String.format(" #$%02X", mask);

			// ANDCC : CC = CC & immediate
			// Permet de DÉSACTIVER (clear) des flags spécifiques
			int newCC = registres.getCC() & mask;
			registres.setCC(newCC);

			cycles = 3;
			break;
		}
		// ========================================
		// BITA - Bit Test A with Memory
		// ========================================
		case 0x85:
		case 0x95:
		case 0xA5:
		case 0xB5: {
			mnemonique = "BITA";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// BITA effectue A & M mais ne stocke pas le résultat
			// Seuls les flags sont modifiés
			int result = registres.getA() & addr.value;

			// Mise à jour des flags (comme ANDA mais sans modifier A)
			updateNZFlags(result);
			registres.setFlag(Registres.CC_V, false); // V toujours à 0

			cycles = addr.cycles;
			break;
		}

		// ========================================
		// BITB - Bit Test B with Memory
		// ========================================
		case 0xC5:
		case 0xD5:
		case 0xE5:
		case 0xF5: {
			mnemonique = "BITB";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// BITB effectue B & M mais ne stocke pas le résultat
			// Seuls les flags sont modifiés
			int result = registres.getB() & addr.value;

			// Mise à jour des flags (comme ANDB mais sans modifier B)
			updateNZFlags(result);
			registres.setFlag(Registres.CC_V, false); // V toujours à 0

			cycles = addr.cycles;
			break;
		}
		// ============================================
		// EORA - EXCLUSIVE OR A WITH MEMORY
		// ============================================
		case 0x88:
		case 0x98:
		case 0xA8:
		case 0xB8: {
			mnemonique = "EORA";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// XOR logique : A = A ^ M
			int result = registres.getA() ^ addr.value;
			registres.setA(result);

			// Mise à jour des flags
			updateNZFlags(result);
			registres.setFlag(Registres.CC_V, false); // V toujours à 0

			cycles = addr.cycles;
			break;
		}
		// ========================================
		// EORB - EXCLUSIVE OR B WITH MEMORY
		// ========================================
		case 0xC8:
		case 0xD8:
		case 0xE8:
		case 0xF8: {
			mnemonique = "EORB";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// XOR logique : B = B ^ M
			int result = registres.getB() ^ addr.value;
			registres.setB(result);

			// Mise à jour des flags
			updateNZFlags(result);
			registres.setFlag(Registres.CC_V, false); // V toujours à 0
			cycles = addr.cycles;
			break;
		}
		// ========================================
		// LEAX - Load Effective Address into X
		// ========================================
		case 0x30: {
			mnemonique = "LEAX";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			// LEAX charge l'adresse effective (pas la valeur)
			registres.setX(addr.address);

			// Mise à jour des flags Z uniquement
			registres.setFlag(Registres.CC_Z, addr.address == 0);

			cycles = addr.cycles + 1;
			break;
		}

		// ========================================
		// LEAY - Load Effective Address into Y
		// ========================================
		case 0x31: {
			mnemonique = "LEAY";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			// LEAY charge l'adresse effective (pas la valeur)
			registres.setY(addr.address);

			// Mise à jour des flags Z uniquement
			registres.setFlag(Registres.CC_Z, addr.address == 0);

			cycles = addr.cycles + 1;
			break;
		}

		// ========================================
		// LEAS - Load Effective Address into S
		// ========================================
		case 0x32: {
			mnemonique = "LEAS";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			// LEAS charge l'adresse effective (pas la valeur)
			registres.setS(addr.address);

			// LEAS n'affecte aucun flag

			cycles = addr.cycles + 1;
			break;
		}

		// ========================================
		// LEAU - Load Effective Address into U
		// ========================================
		case 0x33: {
			mnemonique = "LEAU";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			// LEAU charge l'adresse effective (pas la valeur)
			registres.setU(addr.address);

			// LEAU n'affecte aucun flag

			cycles = addr.cycles + 1;
			break;
		}

		// ========================================
		// EXG - Exchange Registers
		// ========================================
		case 0x1E: {
			mnemonique = "EXG";
			int postbyte = fetchByte();

			int reg1Code = (postbyte >> 4) & 0x0F;
			int reg2Code = postbyte & 0x0F;

			String reg1Name = getRegisterName(reg1Code);
			String reg2Name = getRegisterName(reg2Code);
			operande = " " + reg1Name + "," + reg2Name;

			// Échanger les registres
			int temp = getRegisterValue(reg1Code);
			setRegisterValue(reg1Code, getRegisterValue(reg2Code));
			setRegisterValue(reg2Code, temp);

			cycles = 8;
			break;
		}
		// ========================================
		// ASLA - Arithmetic Shift Left A
		// ========================================
		case 0x48: {
			mnemonique = "ASLA/LSLA";
			int oldA = registres.getA();
			int result = (oldA << 1) & 0xFF;

			registres.setA(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (oldA & 0x80) != 0); // Carry = bit 7 avant décalage
			registres.setFlag(Registres.CC_V, ((oldA & 0x80) != 0) != ((result & 0x80) != 0)); // V = N ⊕ C

			cycles = 2;
			break;
		}

		// ========================================
		// ASLB - Arithmetic Shift Left B
		// ========================================
		case 0x58: {
			mnemonique = "ASLB/LSLB";
			int oldB = registres.getB();
			int result = (oldB << 1) & 0xFF;

			registres.setB(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (oldB & 0x80) != 0); // Carry = bit 7 avant décalage
			registres.setFlag(Registres.CC_V, ((oldB & 0x80) != 0) != ((result & 0x80) != 0)); // V = N ⊕ C

			cycles = 2;
			break;
		}

		// ========================================
		// ASL mémoire - Direct mode (0x08)
		// ========================================
		case 0x08: {
			mnemonique = "ASL";
			int directAddr = fetchByte();
			int address = (registres.getDP() << 8) | directAddr;
			operande = String.format(" $%02X", directAddr);

			int value = mem.getByte(address) & 0xFF;
			int result = (value << 1) & 0xFF;
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x80) != 0);
			registres.setFlag(Registres.CC_V, ((value & 0x80) != 0) != ((result & 0x80) != 0));

			cycles = 6;
			break;
		}

		// ========================================
		// ASL mémoire - Indexed mode (0x68)
		// ========================================
		case 0x68: {
			mnemonique = "ASL";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			int value = mem.getByte(addr.address) & 0xFF;
			int result = (value << 1) & 0xFF;
			mem.setByte(addr.address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x80) != 0);
			registres.setFlag(Registres.CC_V, ((value & 0x80) != 0) != ((result & 0x80) != 0));

			cycles = addr.cycles + 2;
			break;
		}

		// ========================================
		// ASL mémoire - Extended mode (0x78)
		// ========================================
		case 0x78: {
			mnemonique = "ASL";
			int address = fetchWord();
			operande = String.format(" $%04X", address);

			int value = mem.getByte(address) & 0xFF;
			int result = (value << 1) & 0xFF;
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x80) != 0);
			registres.setFlag(Registres.CC_V, ((value & 0x80) != 0) != ((result & 0x80) != 0));

			cycles = 7;
			break;
		}

		// ========================================
		// ASRA - Arithmetic Shift Right A
		// ========================================
		case 0x47: {
			mnemonique = "ASRA";
			int oldA = registres.getA();
			int signBit = oldA & 0x80; // Conserver le bit de signe
			int result = (oldA >> 1) | signBit; // Décalage avec maintien du signe

			registres.setA(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (oldA & 0x01) != 0); // Carry = bit 0 avant décalage

			cycles = 2;
			break;
		}

		// ========================================
		// ASRB - Arithmetic Shift Right B (déjà présent mais incomplet)
		// ========================================
		case 0x57: {
			mnemonique = "ASRB";
			int oldB = registres.getB();
			int signBit = oldB & 0x80; // Conserver le bit de signe
			int result = (oldB >> 1) | signBit; // Décalage avec maintien du signe

			registres.setB(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (oldB & 0x01) != 0); // Carry = bit 0 avant décalage

			cycles = 2;
			break;
		}

		// ========================================
		// ASR mémoire - Direct mode (0x07)
		// ========================================
		case 0x07: {
			mnemonique = "ASR";
			int directAddr = fetchByte();
			int address = (registres.getDP() << 8) | directAddr;
			operande = String.format(" $%02X", directAddr);

			int value = mem.getByte(address) & 0xFF;
			int signBit = value & 0x80;
			int result = (value >> 1) | signBit;
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x01) != 0);

			cycles = 6;
			break;
		}

		// ========================================
		// ASR mémoire - Indexed mode (0x67)
		// ========================================
		case 0x67: {
			mnemonique = "ASR";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			int value = mem.getByte(addr.address) & 0xFF;
			int signBit = value & 0x80;
			int result = (value >> 1) | signBit;
			mem.setByte(addr.address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x01) != 0);

			cycles = addr.cycles + 2;
			break;
		}

		// ========================================
		// ASR mémoire - Extended mode (0x77)
		// ========================================
		case 0x77: {
			mnemonique = "ASR";
			int address = fetchWord();
			operande = String.format(" $%04X", address);

			int value = mem.getByte(address) & 0xFF;
			int signBit = value & 0x80;
			int result = (value >> 1) | signBit;
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x01) != 0);

			cycles = 7;
			break;
		}
		// ========================================
		// LSRA - Logical Shift Right A
		// ========================================
		case 0x44: {
			mnemonique = "LSRA";
			int oldA = registres.getA();
			int result = (oldA >> 1) & 0x7F; // Shift logique (pas de signe)

			registres.setA(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, false); // N toujours 0 (bit 7 = 0)
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (oldA & 0x01) != 0); // Carry = bit 0

			cycles = 2;
			break;
		}

		// ========================================
		// LSRB - Logical Shift Right B
		// ========================================
		case 0x54: {
			mnemonique = "LSRB";
			int oldB = registres.getB();
			int result = (oldB >> 1) & 0x7F; // Shift logique (pas de signe)

			registres.setB(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, false); // N toujours 0
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (oldB & 0x01) != 0);

			cycles = 2;
			break;
		}

		// ========================================
		// LSR mémoire - Direct mode (0x04)
		// ========================================
		case 0x04: {
			mnemonique = "LSR";
			int directAddr = fetchByte();
			int address = (registres.getDP() << 8) | directAddr;
			operande = String.format(" $%02X", directAddr);

			int value = mem.getByte(address) & 0xFF;
			int result = (value >> 1) & 0x7F;
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, false);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x01) != 0);

			cycles = 6;
			break;
		}

		// ========================================
		// LSR mémoire - Indexed mode (0x64)
		// ========================================
		case 0x64: {
			mnemonique = "LSR";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			int value = mem.getByte(addr.address) & 0xFF;
			int result = (value >> 1) & 0x7F;
			mem.setByte(addr.address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, false);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x01) != 0);

			cycles = addr.cycles + 2;
			break;
		}

		// ========================================
		// LSR mémoire - Extended mode (0x74)
		// ========================================
		case 0x74: {
			mnemonique = "LSR";
			int address = fetchWord();
			operande = String.format(" $%04X", address);

			int value = mem.getByte(address) & 0xFF;
			int result = (value >> 1) & 0x7F;
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, false);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x01) != 0);

			cycles = 7;
			break;
		}
		// ============================================
		// COM - COMPLEMENT MEMORY
		// ============================================
		case 0x03: // COM direct
		case 0x63: // COM indexed
		case 0x73: // COM extended
		{
			mnemonique = "COM";
			AddressingResult addr = decodeShiftRotate(opcode);
			operande = " " + addr.description;

			int result = (~addr.value) & 0xFF; // Inversion des bits

			mem.setByte(addr.address, (byte) result);
			updateNZFlags(result);

			registres.setFlag(Registres.CC_V, false); // V toujours 0
			registres.setFlag(Registres.CC_C, true); // C toujours 1

			cycles = addr.cycles;
			break;
		}
		// ============================================
		// DEC - DECREMENT MEMORY
		// ============================================
		case 0x0A: // DEC direct
		case 0x6A: // DEC indexed
		case 0x7A: // DEC extended
		{
			mnemonique = "DEC";
			AddressingResult addr = decodeShiftRotate(opcode);
			operande = " " + addr.description;

			int oldValue = addr.value;
			int result = (oldValue - 1) & 0xFF; // Décrémenter

			mem.setByte(addr.address, (byte) result);
			updateNZFlags(result);

			// V = 1 si passage de $80 à $7F (overflow signé)
			registres.setFlag(Registres.CC_V, oldValue == 0x80);

			cycles = addr.cycles;
			break;
		}
		case 0x80:
		case 0x90:
		case 0xA0:
		case 0xB0: {
			mnemonique = "SUBA";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;
			int oldA = registres.getA();
			int diff = oldA - addr.value;
			registres.setA(diff);
			updateNZVCFlagsSub8(oldA, addr.value, diff);
			cycles = addr.cycles;
			break;
		}
		case 0xC0:
		case 0xD0:
		case 0xE0:
		case 0xF0: {
			mnemonique = "SUBB";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;
			int oldB = registres.getB();
			int diff = oldB - addr.value;
			registres.setB(diff);
			updateNZVCFlagsSub8(oldB, addr.value, diff);
			cycles = addr.cycles;
			break;
		}
		case 0x83:
		case 0x93:
		case 0xA3:
		case 0xB3: {
			mnemonique = "SUBD";
			AddressingResult addr = decodeAddressing(opcode, true);
			operande = " " + addr.description;

			// SUBD: Soustraire de D (16-bit)
			int oldD = registres.getD();
			int diff = oldD - addr.value;

			// Mettre à jour D avec le résultat
			registres.setD(diff & 0xFFFF);

			// Mettre à jour les flags
			updateNZVCFlagsSub16(oldD, addr.value, diff);

			cycles = addr.cycles;
			break;
		}
		case 0x4F:
			mnemonique = "CLRA";
			registres.setA(0);
			registres.setFlag(Registres.CC_N, false);
			registres.setFlag(Registres.CC_Z, true);
			registres.setFlag(Registres.CC_V, false);
			registres.setFlag(Registres.CC_C, false);
			cycles = 2;
			break;
		case 0x5F:
			mnemonique = "CLRB";
			registres.setB(0);
			registres.setFlag(Registres.CC_N, false);
			registres.setFlag(Registres.CC_Z, true);
			registres.setFlag(Registres.CC_V, false);
			registres.setFlag(Registres.CC_C, false);
			cycles = 2;
			break;
		case 0x43: {
			mnemonique = "COMA";
			int result = (~registres.getA()) & 0xFF; // Inversion des bits

			registres.setA(result);
			updateNZFlags(result);

			registres.setFlag(Registres.CC_V, false); // V toujours 0
			registres.setFlag(Registres.CC_C, true); // C toujours 1 cycles = 2;
			break;
		}
		case 0x53: {
			mnemonique = "COMB";
			int result = (~registres.getB()) & 0xFF; // Inversion des bits

			registres.setB(result);
			updateNZFlags(result);

			registres.setFlag(Registres.CC_V, false); // V toujours 0
			registres.setFlag(Registres.CC_C, true); // C toujours 1

			cycles = 2;
			break;
		}
		case 0x4C: {
			mnemonique = "INCA";
			int newA = (registres.getA() + 1) & 0xFF;
			registres.setA(newA);
			updateNZFlags(newA);
			registres.setFlag(Registres.CC_V, newA == 0x80);
			cycles = 2;
			break;
		}
		case 0x5C: {
			mnemonique = "INCB";
			int newB = (registres.getB() + 1) & 0xFF;
			registres.setB(newB);
			updateNZFlags(newB);
			registres.setFlag(Registres.CC_V, newB == 0x80);
			cycles = 2;
			break;
		}
		// ============================================
		// INC - INCREMENT MEMORY
		// ============================================
		case 0x0C: // INC direct
		case 0x6C: // INC indexed
		case 0x7C: // INC extended
		{
			mnemonique = "INC";
			AddressingResult addr = decodeShiftRotate(opcode);
			operande = " " + addr.description;

			int oldValue = addr.value;
			int result = (oldValue + 1) & 0xFF; // Incrémenter

			mem.setByte(addr.address, (byte) result);
			updateNZFlags(result);

			// V = 1 si passage de $7F à $80 (overflow signé)
			registres.setFlag(Registres.CC_V, oldValue == 0x7F);

			cycles = addr.cycles;
			break;
		}
		case 0x4A: {
			mnemonique = "DECA";
			int decA = (registres.getA() - 1) & 0xFF;
			registres.setA(decA);
			updateNZFlags(decA);
			registres.setFlag(Registres.CC_V, decA == 0x7F);
			cycles = 2;
			break;
		}
		case 0x5A: {
			mnemonique = "DECB";
			int decB = (registres.getB() - 1) & 0xFF;
			registres.setB(decB);
			updateNZFlags(decB);
			registres.setFlag(Registres.CC_V, decB == 0x7F);
			cycles = 2;
			break;
		}
		case 0x3D: {
			mnemonique = "MUL";
			// MUL effectue une multiplication non signée: A × B → D
			int result = registres.getA() * registres.getB();

			// Stocker le résultat dans D (16-bit)
			registres.setD(result & 0xFFFF);

			// Mise à jour des flags:
			// Z: mis à 1 si le résultat est 0
			registres.setFlag(Registres.CC_Z, (result & 0xFFFF) == 0);

			// C: mis à 1 si le bit 7 du résultat est à 1 (carry de l'octet bas)
			registres.setFlag(Registres.CC_C, (result & 0x80) != 0);

			// N et V ne sont pas affectés par MUL selon la doc 6809

			cycles = 11; // MUL prend 11 cycles
			break;
		}
		case 0x40: {
			mnemonique = "NEGA";
			// NEG: Complément à deux = 0 - A
			int oldA = registres.getA();
			int result = (0 - oldA) & 0xFF;
			registres.setA(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_V, result == 0x80); // Overflow si résultat = -128
			registres.setFlag(Registres.CC_C, result != 0); // Carry si résultat ≠ 0

			cycles = 2;
			break;
		}

		case 0x50: {
			mnemonique = "NEGB";
			// NEG: Complément à deux = 0 - B
			int oldB = registres.getB();
			int result = (0 - oldB) & 0xFF;
			registres.setB(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_V, result == 0x80); // Overflow si résultat = -128
			registres.setFlag(Registres.CC_C, result != 0); // Carry si résultat ≠ 0

			cycles = 2;
			break;
		}

		// NEG mémoire - Direct mode ($00)
		case 0x00: {
			mnemonique = "NEG";
			int directAddr = fetchByte();
			int address = (registres.getDP() << 8) | directAddr;
			operande = String.format(" $%02X", directAddr);

			int value = mem.getByte(address) & 0xFF;
			int result = (0 - value) & 0xFF;
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_V, result == 0x80);
			registres.setFlag(Registres.CC_C, result != 0);

			cycles = 6;
			break;
		}

		// NEG mémoire - Indexed mode ($60)
		case 0x60: {
			mnemonique = "NEG";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			int value = mem.getByte(addr.address) & 0xFF;
			int result = (0 - value) & 0xFF;
			mem.setByte(addr.address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_V, result == 0x80);
			registres.setFlag(Registres.CC_C, result != 0);

			cycles = addr.cycles + 2;
			break;
		}

		// NEG mémoire - Extended mode ($70)
		case 0x70: {
			mnemonique = "NEG";
			int address = fetchWord();
			operande = String.format(" $%04X", address);

			int value = mem.getByte(address) & 0xFF;
			int result = (0 - value) & 0xFF;
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_V, result == 0x80);
			registres.setFlag(Registres.CC_C, result != 0);

			cycles = 7;
			break;
		}
		case 0x8A:
		case 0x9A:
		case 0xAA:
		case 0xBA: {
			mnemonique = "ORA";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// OR logique : A = A | M
			int result = registres.getA() | addr.value;
			registres.setA(result);

			// Mise à jour des flags
			updateNZFlags(result);
			registres.setFlag(Registres.CC_V, false); // V toujours à 0

			cycles = addr.cycles;
			break;
		}
		case 0xCA:
		case 0xDA:
		case 0xEA:
		case 0xFA: {
			mnemonique = "ORB";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// OR logique : B = B | M
			int result = registres.getB() | addr.value;
			registres.setB(result);

			// Mise à jour des flags
			updateNZFlags(result);
			registres.setFlag(Registres.CC_V, false); // V toujours à 0

			cycles = addr.cycles;
			break;
		}
		case 0x1A: {
			mnemonique = "ORCC";
			int mask = fetchByte();
			operande = String.format(" #$%02X", mask);

			// ORCC : CC = CC | immediate
			// Permet d'ACTIVER (set) des flags spécifiques
			int newCC = registres.getCC() | mask;
			registres.setCC(newCC);

			cycles = 3;
			break;
		}
		// ========================================
		// CLR - Clear Memory (Direct mode)
		// ========================================
		case 0x0F: {
			mnemonique = "CLR";
			int directAddr = fetchByte();
			int address = (registres.getDP() << 8) | directAddr;
			operande = String.format(" $%02X", directAddr);

			// Écrire 0 en mémoire
			mem.setByte(address, (byte) 0x00);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, false); // N = 0
			registres.setFlag(Registres.CC_Z, true); // Z = 1 (résultat = 0)
			registres.setFlag(Registres.CC_V, false); // V = 0
			registres.setFlag(Registres.CC_C, false); // C = 0

			cycles = 6;
			break;
		}

		// ========================================
		// CLR - Clear Memory (Indexed mode)
		// ========================================
		case 0x6F: {
			mnemonique = "CLR";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			// Écrire 0 en mémoire
			mem.setByte(addr.address, (byte) 0x00);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, false); // N = 0
			registres.setFlag(Registres.CC_Z, true); // Z = 1 (résultat = 0)
			registres.setFlag(Registres.CC_V, false); // V = 0
			registres.setFlag(Registres.CC_C, false); // C = 0

			cycles = addr.cycles + 2;
			break;
		}

		// ========================================
		// CLR - Clear Memory (Extended mode)
		// ========================================
		case 0x7F: {
			mnemonique = "CLR";
			int address = fetchWord();
			operande = String.format(" $%04X", address);

			// Écrire 0 en mémoire
			mem.setByte(address, (byte) 0x00);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, false); // N = 0
			registres.setFlag(Registres.CC_Z, true); // Z = 1 (résultat = 0)
			registres.setFlag(Registres.CC_V, false); // V = 0
			registres.setFlag(Registres.CC_C, false); // C = 0

			cycles = 7;
			break;
		}
		// ========================================
		// SBCA - Subtract with Carry from A
		// ========================================
		case 0x82:
		case 0x92:
		case 0xA2:
		case 0xB2: {
			mnemonique = "SBCA";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// SBCA: A = A - M - C
			int oldA = registres.getA();
			int carry = registres.getFlag(Registres.CC_C) ? 1 : 0;
			int diff = oldA - addr.value - carry;

			registres.setA(diff);

			// Mise à jour des flags
			updateNZVCFlagsSub8(oldA, addr.value + carry, diff);

			cycles = addr.cycles;
			break;
		}

		// ========================================
		// SBCB - Subtract with Carry from B
		// ========================================
		case 0xC2:
		case 0xD2:
		case 0xE2:
		case 0xF2: {
			mnemonique = "SBCB";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			// SBCB: B = B - M - C
			int oldB = registres.getB();
			int carry = registres.getFlag(Registres.CC_C) ? 1 : 0;
			int diff = oldB - addr.value - carry;

			registres.setB(diff);

			// Mise à jour des flags
			updateNZVCFlagsSub8(oldB, addr.value + carry, diff);

			cycles = addr.cycles;
			break;
		}

		// ========================================
		// PSHS - Push registers onto S stack
		// ========================================
		case 0x34: {
			mnemonique = "PSHS";
			int mask = fetchByte();
			operande = " " + formatRegisterMask(mask);

			// Push dans l'ordre: PC, U, Y, X, DP, B, A, CC
			if ((mask & 0x80) != 0)
				pushStack16(registres.getPC());
			if ((mask & 0x40) != 0)
				pushStack16(registres.getU());
			if ((mask & 0x20) != 0)
				pushStack16(registres.getY());
			if ((mask & 0x10) != 0)
				pushStack16(registres.getX());
			if ((mask & 0x08) != 0)
				pushStack8(registres.getDP());
			if ((mask & 0x04) != 0)
				pushStack8(registres.getB());
			if ((mask & 0x02) != 0)
				pushStack8(registres.getA());
			if ((mask & 0x01) != 0)
				pushStack8(registres.getCC());

			cycles = 5 + countBitsSet(mask);
			break;
		}

		// ========================================
		// PULS - Pull registers from S stack
		// ========================================
		case 0x35: {
			mnemonique = "PULS";
			int mask = fetchByte();
			operande = " " + formatRegisterMask(mask);

			// Pull dans l'ordre inverse: CC, A, B, DP, X, Y, U, PC
			if ((mask & 0x01) != 0)
				registres.setCC(pullStack8());
			if ((mask & 0x02) != 0)
				registres.setA(pullStack8());
			if ((mask & 0x04) != 0)
				registres.setB(pullStack8());
			if ((mask & 0x08) != 0)
				registres.setDP(pullStack8());
			if ((mask & 0x10) != 0)
				registres.setX(pullStack16());
			if ((mask & 0x20) != 0)
				registres.setY(pullStack16());
			if ((mask & 0x40) != 0)
				registres.setU(pullStack16());
			if ((mask & 0x80) != 0)
				registres.setPC(pullStack16());

			cycles = 5 + countBitsSet(mask);
			break;
		}

		// ========================================
		// PSHU - Push registers onto U stack
		// ========================================
		case 0x36: {
			mnemonique = "PSHU";
			int mask = fetchByte();
			operande = " " + formatRegisterMask(mask);

			// Push dans l'ordre: PC, S, Y, X, DP, B, A, CC
			if ((mask & 0x80) != 0)
				pushStackU16(registres.getPC());
			if ((mask & 0x40) != 0)
				pushStackU16(registres.getS()); // S pour PSHU
			if ((mask & 0x20) != 0)
				pushStackU16(registres.getY());
			if ((mask & 0x10) != 0)
				pushStackU16(registres.getX());
			if ((mask & 0x08) != 0)
				pushStackU8(registres.getDP());
			if ((mask & 0x04) != 0)
				pushStackU8(registres.getB());
			if ((mask & 0x02) != 0)
				pushStackU8(registres.getA());
			if ((mask & 0x01) != 0)
				pushStackU8(registres.getCC());

			cycles = 5 + countBitsSet(mask);
			break;
		}

		// ========================================
		// TFR - Transfer Register to Register
		// ========================================
		case 0x1F: {
			mnemonique = "TFR";
			int postbyte = fetchByte();

			int reg1Code = (postbyte >> 4) & 0x0F;
			int reg2Code = postbyte & 0x0F;

			String reg1Name = getRegisterName(reg1Code);
			String reg2Name = getRegisterName(reg2Code);
			operande = " " + reg1Name + "," + reg2Name;

			// Copier la valeur de reg1 vers reg2
			int value = getRegisterValue(reg1Code);
			setRegisterValue(reg2Code, value);

			cycles = 6;
			break;
		}

		// ========================================
		// JMP - Jump (indexed mode)
		// ========================================
		case 0x6E: {
			mnemonique = "JMP";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			// JMP change directement le PC
			registres.setPC(addr.address);

			cycles = addr.cycles;
			break;
		}

		// ========================================
		// JMP - Jump (extended mode)
		// ========================================
		case 0x7E: {
			mnemonique = "JMP";
			int address = fetchWord();
			operande = String.format(" $%04X", address);

			// JMP change directement le PC
			registres.setPC(address);

			cycles = 3;
			break;
		}
		// ========================================
		// BRA - Branch Always (indexed mode)
		// ========================================
		case 0x20: {
		    mnemonique = "BRA";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    // Saut direct vers l'adresse indexée
		    registres.setPC(addr.address);

		    cycles = addr.cycles;
		    break;
		}
		// ========================================
		// BRN - Branch Never (indexed mode)
		// ========================================
		// ========================================
		// BRN - Branch Never (indexed mode)
		// ========================================
		// ========================================
		// BRN - Branch Never (indexed mode)
		// ========================================
		// ========================================
		// BRN - Branch Never (indexed mode)
		// gère : BRN ,X  ET  BRN 5,X
		// ========================================
		case 0x21: {
		    mnemonique = "BRN";
		    AddressingResult addr = decodeIndexed(false);
		    
		    // Construction de l'opérande
		    if (addr.description == null || addr.description.trim().isEmpty()) {
		        operande = ",X";
		    } else {
		        // Supprime l'espace devant si présent
		        String desc = addr.description.trim();
		        if (desc.startsWith(" ")) {
		            desc = desc.substring(1);
		        }
		        operande = desc;
		    }
		    
		    // S'assurer que operande n'est jamais null
		    if (operande == null) {
		        operande = "";
		    }
		    
		    cycles = addr.cycles;
		    break;
		}

		// ========================================
		// BSR - Branch to Subroutine (indexed mode)
		// ========================================
		case 0x8D: {
		    mnemonique = "BSR";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    // Empiler l'adresse de retour
		    pushStack16(registres.getPC());

		    // Saut vers l'adresse indexée
		    registres.setPC(addr.address);

		    cycles = addr.cycles + 2;
		    break;
		}
		case 0x24: { // BCC (indexé)
		    mnemonique = "BCC";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    // bit C = 0 ?
		    if ((registres.getCC() & Registres.CC_C) == 0) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}
		case 0x25: { // BCS (indexé)
		    mnemonique = "BCS";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    // bit C = 1 ?
		    if ((registres.getCC() & Registres.CC_C) != 0) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}
		case 0x27: { // BEQ (indexé)
		    mnemonique = "BEQ";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    // bit Z = 1 ?
		    if ((registres.getCC() & Registres.CC_Z) != 0) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}
		// ========================================
		// BNE - Branch if Not Equal (indexed mode)
		// ========================================
		case 0x26: {
		    mnemonique = "BNE";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    if ((registres.getCC() & Registres.CC_Z) == 0) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}
		case 0x2A: {
		    mnemonique = "BPL";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    if ((registres.getCC() & Registres.CC_N) == 0) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}
		case 0x2B: {
		    mnemonique = "BMI";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    if ((registres.getCC() & Registres.CC_N) != 0) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		} 
		// ========================================
		// BVC - Branch if Overflow Clear (indexed mode)
		// ========================================
		case 0x28: {
		    mnemonique = "BVC";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    if ((registres.getCC() & Registres.CC_V) == 0) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}
		// ========================================
		// BVS - Branch if Overflow Set (indexed mode)
		// ========================================
		case 0x29: {
		    mnemonique = "BVS";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    if ((registres.getCC() & Registres.CC_V) != 0) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}

		// ========================================
		// BHI - Branch if Higher (indexed mode)
		// ========================================
		case 0x22: {
		    mnemonique = "BHI";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    if (((registres.getCC() & Registres.CC_C) == 0) &&
		        ((registres.getCC() & Registres.CC_Z) == 0)) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}

		// ========================================
		// BLS - Branch if Lower or Same (indexed mode)
		// ========================================
		case 0x23: {
		    mnemonique = "BLS";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    if (((registres.getCC() & Registres.CC_C) != 0) ||
		        ((registres.getCC() & Registres.CC_Z) != 0)) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}
		// ========================================
		// BGE - Branch if Greater or Equal (indexed mode)
		// ========================================
		case 0x2C: {
		    mnemonique = "BGE";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    boolean N = (registres.getCC() & Registres.CC_N) != 0;
		    boolean V = (registres.getCC() & Registres.CC_V) != 0;

		    if (N == V) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}
		// ========================================
		// BLT - Branch if Less Than (indexed mode)
		// ========================================
		case 0x2D: {
		    mnemonique = "BLT";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    boolean N = (registres.getCC() & Registres.CC_N) != 0;
		    boolean V = (registres.getCC() & Registres.CC_V) != 0;

		    if (N != V) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}
		// ========================================
		// BGT - Branch if Greater Than (indexed mode)
		// ========================================
		case 0x2E: {
		    mnemonique = "BGT";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    boolean Z = (registres.getCC() & Registres.CC_Z) != 0;
		    boolean N = (registres.getCC() & Registres.CC_N) != 0;
		    boolean V = (registres.getCC() & Registres.CC_V) != 0;

		    if (!Z && (N == V)) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}
		// ========================================
		// BLE - Branch if Less or Equal (indexed mode)
		// ========================================
		case 0x2F: {
		    mnemonique = "BLE";
		    AddressingResult addr = decodeIndexed(false);
		    operande = " " + addr.description;

		    boolean Z = (registres.getCC() & Registres.CC_Z) != 0;
		    boolean N = (registres.getCC() & Registres.CC_N) != 0;
		    boolean V = (registres.getCC() & Registres.CC_V) != 0;

		    if (Z || (N != V)) {
		        registres.setPC(addr.address);
		    }

		    cycles = addr.cycles + 1;
		    break;
		}









		
// ========================================
		// JSR - Jump to Subroutine (indexed mode)
		// ========================================
		case 0xAD: {
			mnemonique = "JSR";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			// Empiler l'adresse de retour (PC actuel)
			pushStack16(registres.getPC());

			// Sauter à l'adresse effective
			registres.setPC(addr.address);

			cycles = addr.cycles + 2;
			break;
		}

		// ========================================
		// JSR - Jump to Subroutine (extended mode)
		// ========================================
		case 0xBD: {
			mnemonique = "JSR";
			int address = fetchWord();
			operande = String.format(" $%04X", address);

			// Empiler l'adresse de retour (PC actuel)
			pushStack16(registres.getPC());

			// Sauter à l'adresse
			registres.setPC(address);

			cycles = 7;
			break;
		}

		// ========================================
		// RTS - Return from Subroutine
		// ========================================
		case 0x39: {
			mnemonique = "RTS";

			// Dépiler l'adresse de retour
			int returnAddress = pullStack16();
			registres.setPC(returnAddress);

			cycles = 5;
			break;
		}

		// ========================================
		// RTI - Return from Interrupt
		// ========================================
		case 0x3B: {
			mnemonique = "RTI";

			// Dépiler tous les registres (dans l'ordre inverse de l'empilement)
			registres.setCC(pullStack8());

			// Vérifier le flag E (Entire state saved)
			if (registres.getFlag(Registres.CC_E)) {
				// Tous les registres ont été sauvegardés
				registres.setA(pullStack8());
				registres.setB(pullStack8());
				registres.setDP(pullStack8());
				registres.setX(pullStack16());
				registres.setY(pullStack16());
				registres.setU(pullStack16());
				cycles = 15;
			} else {
				// Seulement CC et PC ont été sauvegardés (interruption rapide)
				cycles = 6;
			}

			registres.setPC(pullStack16());
			break;
		}

		// ========================================
		// SEX - Sign Extend (B -> A)
		// ========================================
		case 0x1D: {
			mnemonique = "SEX";

			// SEX étend le signe de B (8-bit) vers D (16-bit)
			int b = registres.getB();
			if ((b & 0x80) != 0) {
				// B est négatif, mettre A à 0xFF
				registres.setA(0xFF);
			} else {
				// B est positif, mettre A à 0x00
				registres.setA(0x00);
			}

			// Mise à jour des flags
			updateNZFlags16(registres.getD());

			cycles = 2;
			break;
		}

		// ========================================
		// PULU - Pull registers from U stack
		// ========================================
		case 0x37: {
			mnemonique = "PULU";
			int mask = fetchByte();
			operande = " " + formatRegisterMask(mask);

			// Pull dans l'ordre inverse: CC, A, B, DP, X, Y, S, PC
			if ((mask & 0x01) != 0)
				registres.setCC(pullStackU8());
			if ((mask & 0x02) != 0)
				registres.setA(pullStackU8());
			if ((mask & 0x04) != 0)
				registres.setB(pullStackU8());
			if ((mask & 0x08) != 0)
				registres.setDP(pullStackU8());
			if ((mask & 0x10) != 0)
				registres.setX(pullStackU16());
			if ((mask & 0x20) != 0)
				registres.setY(pullStackU16());
			if ((mask & 0x40) != 0)
				registres.setS(pullStackU16()); // S pour PULU
			if ((mask & 0x80) != 0)
				registres.setPC(pullStackU16());

			cycles = 5 + countBitsSet(mask);
			break;
		}

		case 0x81:
		case 0x91:
		case 0xA1:
		case 0xB1: {
			mnemonique = "CMPA";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			int oldA = registres.getA();
			int result = oldA - addr.value;

			updateNZVCFlagsSub8(oldA, addr.value, result);

			cycles = addr.cycles;
			break;
		}

		case 0xC1:
		case 0xD1:
		case 0xE1:
		case 0xF1: {
			mnemonique = "CMPB";
			AddressingResult addr = decodeAddressing(opcode, false);
			operande = " " + addr.description;

			int oldB = registres.getB();
			int result = oldB - addr.value;

			updateNZVCFlagsSub8(oldB, addr.value, result);

			cycles = addr.cycles;
			break;
		}
		case 0x8C:
		case 0x9C:
		case 0xAC:
		case 0xBC: {
			mnemonique = "CMPX";
			AddressingResult addr = decodeAddressing(opcode, true);
			operande = " " + addr.description;

			// CMPX: Compare X avec la valeur (soustraction sans stocker)
			int result = registres.getX() - addr.value;

			// Mise à jour des flags (comme une soustraction 16-bit)
			updateNZVCFlagsSub16(registres.getX(), addr.value, result);

			cycles = addr.cycles;
			break;
		}
		case 0x12:
			mnemonique = "NOP";
			cycles = 2;
			break;
		case 0x19: {
			mnemonique = "DAA";

			int a = registres.getA();
			int correction = 0;

			// Vérifier le nibble bas (bits 0-3)
			boolean halfCarry = registres.getFlag(Registres.CC_H);
			if ((a & 0x0F) > 0x09 || halfCarry) {
				correction += 0x06;
			}

			// Vérifier le nibble haut (bits 4-7)
			boolean carry = registres.getFlag(Registres.CC_C);
			if (((a & 0xF0) > 0x90) || carry || (((a & 0xF0) >= 0x90) && ((a & 0x0F) > 0x09))) {
				correction += 0x60;
			}

			// Appliquer la correction
			int result = (a + correction) & 0xFF;
			registres.setA(result);

			// Mise à jour des flags
			updateNZFlags(result);

			// Carry : si correction >= 0x60
			if (correction >= 0x60) {
				registres.setFlag(Registres.CC_C, true);
			}

			// Note : V est indéfini selon le manuel, on le laisse inchangé

			cycles = 2;
			break;
		}
		// ========================================
		// ROLA - Rotate Left A through Carry
		// ========================================
		case 0x49: {
			mnemonique = "ROLA";
			int oldA = registres.getA();
			int oldCarry = registres.getFlag(Registres.CC_C) ? 1 : 0;

			// Rotation : bit 7 → Carry, Carry → bit 0
			int result = ((oldA << 1) | oldCarry) & 0xFF;

			registres.setA(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (oldA & 0x80) != 0); // Bit 7 → Carry
			registres.setFlag(Registres.CC_V, ((oldA & 0x80) != 0) != ((result & 0x80) != 0)); // N ⊕ C

			cycles = 2;
			break;
		}

		// ========================================
		// ROLB - Rotate Left B through Carry
		// ========================================
		case 0x59: {
			mnemonique = "ROLB";
			int oldB = registres.getB();
			int oldCarry = registres.getFlag(Registres.CC_C) ? 1 : 0;

			// Rotation : bit 7 → Carry, Carry → bit 0
			int result = ((oldB << 1) | oldCarry) & 0xFF;

			registres.setB(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (oldB & 0x80) != 0); // Bit 7 → Carry
			registres.setFlag(Registres.CC_V, ((oldB & 0x80) != 0) != ((result & 0x80) != 0)); // N ⊕ C

			cycles = 2;
			break;
		}
		// ========================================
		// ROL - Rotate Left Memory (Direct mode)
		// ========================================
		case 0x09: {
			mnemonique = "ROL";
			int directAddr = fetchByte();
			int address = (registres.getDP() << 8) | directAddr;
			operande = String.format(" $%02X", directAddr);

			int value = mem.getByte(address) & 0xFF;
			int oldCarry = registres.getFlag(Registres.CC_C) ? 1 : 0;

			// Rotation : bit 7 → Carry, Carry → bit 0
			int result = ((value << 1) | oldCarry) & 0xFF;
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x80) != 0);
			registres.setFlag(Registres.CC_V, ((value & 0x80) != 0) != ((result & 0x80) != 0));

			cycles = 6;
			break;
		}

		// ========================================
		// ROL - Rotate Left Memory (Indexed mode)
		// ========================================
		case 0x69: {
			mnemonique = "ROL";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			int value = mem.getByte(addr.address) & 0xFF;
			int oldCarry = registres.getFlag(Registres.CC_C) ? 1 : 0;

			// Rotation : bit 7 → Carry, Carry → bit 0
			int result = ((value << 1) | oldCarry) & 0xFF;
			mem.setByte(addr.address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x80) != 0);
			registres.setFlag(Registres.CC_V, ((value & 0x80) != 0) != ((result & 0x80) != 0));

			cycles = addr.cycles + 2;
			break;
		}

		// ========================================
		// ROL - Rotate Left Memory (Extended mode)
		// ========================================
		case 0x79: {
			mnemonique = "ROL";
			int address = fetchWord();
			operande = String.format(" $%04X", address);

			int value = mem.getByte(address) & 0xFF;
			int oldCarry = registres.getFlag(Registres.CC_C) ? 1 : 0;

			// Rotation : bit 7 → Carry, Carry → bit 0
			int result = ((value << 1) | oldCarry) & 0xFF;
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x80) != 0);
			registres.setFlag(Registres.CC_V, ((value & 0x80) != 0) != ((result & 0x80) != 0));

			cycles = 7;
			break;
		}

		// ========================================
		// RORA - Rotate Right A through Carry
		// ========================================
		case 0x46: {
			mnemonique = "RORA";
			int oldA = registres.getA();
			int oldCarry = registres.getFlag(Registres.CC_C) ? 1 : 0;

			// Rotation : bit 0 → Carry, Carry → bit 7
			int result = (oldA >> 1) | (oldCarry << 7);

			registres.setA(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (oldA & 0x01) != 0); // Bit 0 → Carry

			cycles = 2;
			break;
		}

		// ========================================
		// RORB - Rotate Right B through Carry
		// ========================================
		case 0x56: {
			mnemonique = "RORB";
			int oldB = registres.getB();
			int oldCarry = registres.getFlag(Registres.CC_C) ? 1 : 0;

			// Rotation : bit 0 → Carry, Carry → bit 7
			int result = (oldB >> 1) | (oldCarry << 7);

			registres.setB(result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (oldB & 0x01) != 0); // Bit 0 → Carry

			cycles = 2;
			break;
		}

		// ========================================
		// ROR mémoire - Direct mode (0x06)
		// ========================================
		case 0x06: {
			mnemonique = "ROR";
			int directAddr = fetchByte();
			int address = (registres.getDP() << 8) | directAddr;
			operande = String.format(" $%02X", directAddr);

			int value = mem.getByte(address) & 0xFF;
			int oldCarry = registres.getFlag(Registres.CC_C) ? 1 : 0;

			// Rotation : bit 0 → Carry, Carry → bit 7
			int result = (value >> 1) | (oldCarry << 7);
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x01) != 0);

			cycles = 6;
			break;
		}

		// ========================================
		// ROR mémoire - Indexed mode (0x66)
		// ========================================
		case 0x66: {
			mnemonique = "ROR";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			int value = mem.getByte(addr.address) & 0xFF;
			int oldCarry = registres.getFlag(Registres.CC_C) ? 1 : 0;

			// Rotation : bit 0 → Carry, Carry → bit 7
			int result = (value >> 1) | (oldCarry << 7);
			mem.setByte(addr.address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x01) != 0);

			cycles = addr.cycles + 2;
			break;
		}

		// ========================================
		// ROR mémoire - Extended mode (0x76)
		// ========================================
		case 0x76: {
			mnemonique = "ROR";
			int address = fetchWord();
			operande = String.format(" $%04X", address);

			int value = mem.getByte(address) & 0xFF;
			int oldCarry = registres.getFlag(Registres.CC_C) ? 1 : 0;

			// Rotation : bit 0 → Carry, Carry → bit 7
			int result = (value >> 1) | (oldCarry << 7);
			mem.setByte(address, (byte) result);

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (result & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, result == 0);
			registres.setFlag(Registres.CC_C, (value & 0x01) != 0);

			cycles = 7;
			break;
		}
		case 0x3F:
			mnemonique = "SWI";
			pushStack16(registres.getPC());
			pushStack16(registres.getU());
			pushStack16(registres.getY());
			pushStack16(registres.getX());
			pushStack8(registres.getDP());
			pushStack8(registres.getB());
			pushStack8(registres.getA());
			pushStack8(registres.getCC() | 0x80);
			registres.setFlag(Registres.CC_I, true);
			registres.setFlag(Registres.CC_F, true);
			int swiVector = mem.getWord(0xFFFA);
			registres.setPC(swiVector);
			cycles = 19;
			break;
		case 0x3C: {
			mnemonique = "CWAI";
			int mask = fetchByte();
			operande = String.format(" #$%02X", mask);

			// AND du masque avec CC
			int newCC = registres.getCC() & mask;
			registres.setCC(newCC);

			// Empiler tout l'état (comme SWI)
			pushStack16(registres.getPC());
			pushStack16(registres.getU());
			pushStack16(registres.getY());
			pushStack16(registres.getX());
			pushStack8(registres.getDP());
			pushStack8(registres.getB());
			pushStack8(registres.getA());
			pushStack8(registres.getCC() | 0x80); // Set E flag

			// Mettre le CPU en attente d'interruption
			waitingForInterrupt = true;

			System.out.println("║ ⏸️  CWAI - Attente d'interruption        ║");

			cycles = 20;
			break;
		}
		// ========================================
		// TSTA - Test A
		// ========================================
		case 0x4D: {
			mnemonique = "TSTA";

			// TST effectue une comparaison avec 0 sans modifier le registre
			int value = registres.getA();

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (value & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, value == 0);
			registres.setFlag(Registres.CC_V, false); // V toujours 0
			// C n'est pas affecté

			cycles = 2;
			break;
		}

		// ========================================
		// TSTB - Test B
		// ========================================
		case 0x5D: {
			mnemonique = "TSTB";

			// TST effectue une comparaison avec 0 sans modifier le registre
			int value = registres.getB();

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (value & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, value == 0);
			registres.setFlag(Registres.CC_V, false); // V toujours 0
			// C n'est pas affecté

			cycles = 2;
			break;
		}
		// ========================================
		// TST - Test Memory (Direct mode)
		// ========================================
		case 0x0D: {
			mnemonique = "TST";
			int directAddr = fetchByte();
			int address = (registres.getDP() << 8) | directAddr;
			operande = String.format(" $%02X", directAddr);

			int value = mem.getByte(address) & 0xFF;

			// Mise à jour des flags (comme si on comparait avec 0)
			registres.setFlag(Registres.CC_N, (value & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, value == 0);
			registres.setFlag(Registres.CC_V, false);
			// C n'est pas affecté

			cycles = 6;
			break;
		}

		// ========================================
		// TST - Test Memory (Indexed mode)
		// ========================================
		case 0x6D: {
			mnemonique = "TST";
			AddressingResult addr = decodeIndexed(false);
			operande = " " + addr.description;

			int value = mem.getByte(addr.address) & 0xFF;

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (value & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, value == 0);
			registres.setFlag(Registres.CC_V, false);
			// C n'est pas affecté

			cycles = addr.cycles + 2;
			break;
		}
		// ========================================
		// SYNC -
		// ========================================
		case 0x13: {
			mnemonique = "SYNC";

			// SYNC met le CPU en attente jusqu'à la prochaine interruption
			// Similaire à CWAI mais sans modifier CC
			waitingForInterrupt = true;

			System.out.println("║ ⏸️  SYNC - Attente d'interruption       ║");

			cycles = 4; // Minimum, peut être plus selon l'attente
			break;
		}
		// ========================================
		// TST - Test Memory (Extended mode)
		// ========================================
		case 0x7D: {
			mnemonique = "TST";
			int address = fetchWord();
			operande = String.format(" $%04X", address);

			int value = mem.getByte(address) & 0xFF;

			// Mise à jour des flags
			registres.setFlag(Registres.CC_N, (value & 0x80) != 0);
			registres.setFlag(Registres.CC_Z, value == 0);
			registres.setFlag(Registres.CC_V, false);
			// C n'est pas affecté

			cycles = 7;
			break;
		}
		default:
			mnemonique = "???";
			operande = String.format(" $%02X", opcode);
			System.out.printf("║ ⚠️  OPCODE NON IMPLÉMENTÉ: $%02X         ║\n", opcode);
			cycles = 1;
			break;
		}

		currentInstruction = mnemonique + operande;
		cycleCount += cycles;
		printDebugInfo(mnemonique, operande, cycles);
	}

	private String decodeRegisterList(int postbyte) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;

		if ((postbyte & 0x01) != 0) {
			if (!first)
				sb.append(",");
			sb.append("CC");
			first = false;
		}
		if ((postbyte & 0x02) != 0) {
			if (!first)
				sb.append(",");
			sb.append("A");
			first = false;
		}
		if ((postbyte & 0x04) != 0) {
			if (!first)
				sb.append(",");
			sb.append("B");
			first = false;
		}
		if ((postbyte & 0x08) != 0) {
			if (!first)
				sb.append(",");
			sb.append("DP");
			first = false;
		}
		if ((postbyte & 0x10) != 0) {
			if (!first)
				sb.append(",");
			sb.append("X");
			first = false;
		}
		if ((postbyte & 0x20) != 0) {
			if (!first)
				sb.append(",");
			sb.append("Y");
			first = false;
		}
		if ((postbyte & 0x40) != 0) {
			if (!first)
				sb.append(",");
			sb.append("U/S");
			first = false;
		}
		if ((postbyte & 0x80) != 0) {
			if (!first)
				sb.append(",");
			sb.append("PC");
			first = false;
		}

		return sb.toString();
	}

	// Simule une interruption IRQ ou FIRQ
	// Utilisé pour sortir du mode CWAI
	public void triggerInterrupt(int vectorAddress) {
		if (waitingForInterrupt) {
			System.out.println("🔔 Interruption reçue - Sortie de CWAI");
			waitingForInterrupt = false;

			// Le PC est déjà empilé par CWAI
			// Charger le vecteur d'interruption
			int handlerAddress = mem.getWord(vectorAddress);
			registres.setPC(handlerAddress);
		}
	}

	private String getRegisterName(int code) {
		switch (code) {
		case 0x00:
			return "D";
		case 0x01:
			return "X";
		case 0x02:
			return "Y";
		case 0x03:
			return "U";
		case 0x04:
			return "S";
		case 0x05:
			return "PC";
		case 0x08:
			return "A";
		case 0x09:
			return "B";
		case 0x0A:
			return "CC";
		case 0x0B:
			return "DP";
		default:
			return "?";
		}
	}

	private int getRegisterValue(int code) {
		switch (code) {
		case 0x00:
			return registres.getD();
		case 0x01:
			return registres.getX();
		case 0x02:
			return registres.getY();
		case 0x03:
			return registres.getU();
		case 0x04:
			return registres.getS();
		case 0x05:
			return registres.getPC();
		case 0x08:
			return registres.getA();
		case 0x09:
			return registres.getB();
		case 0x0A:
			return registres.getCC();
		case 0x0B:
			return registres.getDP();
		default:
			return 0;
		}
	}

	private void setRegisterValue(int code, int value) {
		switch (code) {
		case 0x00:
			registres.setD(value & 0xFFFF);
			break;
		case 0x01:
			registres.setX(value & 0xFFFF);
			break;
		case 0x02:
			registres.setY(value & 0xFFFF);
			break;
		case 0x03:
			registres.setU(value & 0xFFFF);
			break;
		case 0x04:
			registres.setS(value & 0xFFFF);
			break;
		case 0x05:
			registres.setPC(value & 0xFFFF);
			break;
		case 0x08:
			registres.setA(value & 0xFF);
			break;
		case 0x09:
			registres.setB(value & 0xFF);
			break;
		case 0x0A:
			registres.setCC(value & 0xFF);
			break;
		case 0x0B:
			registres.setDP(value & 0xFF);
			break;
		}
	}

	// ============================================
	// MÉTHODE HELPER POUR L'AFFICHAGE DEBUG
	// ============================================

	private void printDebugInfo(String mnemonique, String operande, int cycles) {

		// Débogage : Le CPU affiche des informations détaillées à chaque instruction.
		System.out.println("╠════════════════════════════════════════╣");
		System.out.printf("║ %s%-35s ║\n", mnemonique, operande);
		System.out.printf("║ A=$%02X B=$%02X D=$%04X X=$%04X Y=$%04X    ║\n", registres.getA(), registres.getB(),
				registres.getD(), registres.getX(), registres.getY());
		System.out.printf("║ PC après : $%04X   Cycles : +%-3d       ║\n", registres.getPC(), cycles);
		System.out.printf("║ Flags : [%c%c%c%c%c%c%c%c]                     ║\n",
				registres.getFlag(Registres.CC_E) ? 'E' : '.', registres.getFlag(Registres.CC_F) ? 'F' : '.',
				registres.getFlag(Registres.CC_H) ? 'H' : '.', registres.getFlag(Registres.CC_I) ? 'I' : '.',
				registres.getFlag(Registres.CC_N) ? 'N' : '.', registres.getFlag(Registres.CC_Z) ? 'Z' : '.',
				registres.getFlag(Registres.CC_V) ? 'V' : '.', registres.getFlag(Registres.CC_C) ? 'C' : '.');
		System.out.println("╚════════════════════════════════════════╝");
		// ========== DEBUG - FIN ==========
	}
}