package MOTO6809;

import java.util.*;

public class Assembleur {
	private Map<String, Integer> opcodes = new HashMap<>();
	private Map<String, Integer> labels = new HashMap<>();

	public Assembleur() {
		initOpcodes();
	}

	private void initOpcodes() {
		// ========================================
		// LDA - Load A
		// ========================================
		opcodes.put("LDA #", 0x86); // Immediate
		opcodes.put("LDA", 0x96); // Direct
		opcodes.put("LDA indexed", 0xA6); // Indexed
		opcodes.put("LDA ext", 0xB6); // Extended

		// ========================================
		// LDB - Load B
		// ========================================
		opcodes.put("LDB #", 0xC6); // Immediate
		opcodes.put("LDB", 0xD6); // Direct
		opcodes.put("LDB indexed", 0xE6); // Indexed
		opcodes.put("LDB ext", 0xF6); // Extended

		// ========================================
		// LDD - Load D (A:B)
		// ========================================
		opcodes.put("LDD #", 0xCC); // Immediate
		opcodes.put("LDD", 0xDC); // Direct
		opcodes.put("LDD indexed", 0xEC); // Indexed
		opcodes.put("LDD ext", 0xFC); // Extended

		// ========================================
		// LDX - Load X
		// ========================================
		opcodes.put("LDX #", 0x8E); // Immediate
		opcodes.put("LDX", 0x9E); // Direct
		opcodes.put("LDX indexed", 0xAE); // Indexed
		opcodes.put("LDX ext", 0xBE); // Extended

		// ========================================
		// LDY - Load Y (Page 2)
		// ========================================
		opcodes.put("LDY #", 0x108E); // Immediate (page 2: $10 $8E)
		opcodes.put("LDY", 0x109E); // Direct
		opcodes.put("LDY indexed", 0x10AE); // Indexed
		opcodes.put("LDY ext", 0x10BE); // Extended

		// ========================================
		// LDU - Load U
		// ========================================
		opcodes.put("LDU #", 0xCE); // Immediate
		opcodes.put("LDU", 0xDE); // Direct
		opcodes.put("LDU indexed", 0xEE); // Indexed
		opcodes.put("LDU ext", 0xFE); // Extended

		// ========================================
		// LDS - Load S (Page 2)
		// ========================================
		opcodes.put("LDS #", 0x10CE); // Immediate (page 2: $10 $CE)
		opcodes.put("LDS", 0x10DE); // Direct
		opcodes.put("LDS indexed", 0x10EE); // Indexed
		opcodes.put("LDS ext", 0x10FE); // Extended

		// ========================================
		// STA - Store A
		// ========================================
		opcodes.put("STA", 0x97); // Direct
		opcodes.put("STA indexed", 0xA7); // Indexed
		opcodes.put("STA ext", 0xB7); // Extended

		// ========================================
		// STB - Store B
		// ========================================
		opcodes.put("STB", 0xD7); // Direct
		opcodes.put("STB indexed", 0xE7); // Indexed
		opcodes.put("STB ext", 0xF7); // Extended

		// ========================================
		// STD - Store D
		// ========================================
		opcodes.put("STD", 0xDD); // Direct
		opcodes.put("STD indexed", 0xED); // Indexed
		opcodes.put("STD ext", 0xFD); // Extended

		// ========================================
		// STX - Store X
		// ========================================
		opcodes.put("STX", 0x9F); // Direct
		opcodes.put("STX indexed", 0xAF); // Indexed
		opcodes.put("STX ext", 0xBF); // Extended

		// ========================================
		// STU - Store U
		// ========================================
		opcodes.put("STU", 0xDF); // Direct
		opcodes.put("STU indexed", 0xEF); // Indexed
		opcodes.put("STU ext", 0xFF); // Extended

		// ========================================
		// STS - Store S (Page 2)
		// ========================================
		opcodes.put("STS", 0x10DF); // Direct
		opcodes.put("STS indexed", 0x10EF); // Indexed
		opcodes.put("STS ext", 0x10FF); // Extended

		// ========================================
		// STY - Store Y (Page 2)
		// ========================================
		opcodes.put("STY", 0x109F); // Direct
		opcodes.put("STY indexed", 0x10AF); // Indexed
		opcodes.put("STY ext", 0x10BF); // Extended

		// ========================================
		// ADDA - Add to A
		// ========================================
		opcodes.put("ADDA #", 0x8B); // Immediate
		opcodes.put("ADDA", 0x9B); // Direct
		opcodes.put("ADDA indexed", 0xAB); // Indexed
		opcodes.put("ADDA ext", 0xBB); // Extended

		// ========================================
		// ADDB - Add to B
		// ========================================
		opcodes.put("ADDB #", 0xCB); // Immediate
		opcodes.put("ADDB", 0xDB); // Direct
		opcodes.put("ADDB indexed", 0xEB); // Indexed
		opcodes.put("ADDB ext", 0xFB); // Extended

		// ========================================
		// ADDD - Add to D
		// ========================================
		opcodes.put("ADDD #", 0xC3); // Immediate
		opcodes.put("ADDD", 0xD3); // Direct
		opcodes.put("ADDD indexed", 0xE3); // Indexed
		opcodes.put("ADDD ext", 0xF3); // Extended

		// ========================================
		// SUBA - Subtract from A
		// ========================================
		opcodes.put("SUBA #", 0x80); // Immediate
		opcodes.put("SUBA", 0x90); // Direct
		opcodes.put("SUBA indexed", 0xA0); // Indexed
		opcodes.put("SUBA ext", 0xB0); // Extended

		// ========================================
		// SUBB - Subtract from B
		// ========================================
		opcodes.put("SUBB #", 0xC0); // Immediate
		opcodes.put("SUBB", 0xD0); // Direct
		opcodes.put("SUBB indexed", 0xE0); // Indexed
		opcodes.put("SUBB ext", 0xF0); // Extended

		// ========================================
		// SUBD - Subtract from D
		// ========================================
		opcodes.put("SUBD #", 0x83); // Immediate
		opcodes.put("SUBD", 0x93); // Direct
		opcodes.put("SUBD indexed", 0xA3); // Indexed
		opcodes.put("SUBD ext", 0xB3); // Extended

		// ========================================
		// CMPA - Compare A
		// ========================================
		opcodes.put("CMPA #", 0x81); // Immediate
		opcodes.put("CMPA", 0x91); // Direct
		opcodes.put("CMPA indexed", 0xA1); // Indexed
		opcodes.put("CMPA ext", 0xB1); // Extended

		// ========================================
		// CMPB - Compare B
		// ========================================
		opcodes.put("CMPB #", 0xC1); // Immediate
		opcodes.put("CMPB", 0xD1); // Direct
		opcodes.put("CMPB indexed", 0xE1); // Indexed
		opcodes.put("CMPB ext", 0xF1); // Extended

		// ========================================
		// CMPD - Compare D (page 2)
		// ========================================
		opcodes.put("CMPD #", 0x1083); // Immediate
		opcodes.put("CMPD", 0x1093); // Direct
		opcodes.put("CMPD indexed", 0x10A3); // Indexed
		opcodes.put("CMPD ext", 0x10B3); // Extended

		// ========================================
		// CMPX - Compare X
		// ========================================
		opcodes.put("CMPX #", 0x8C); // Immediate
		opcodes.put("CMPX", 0x9C); // Direct
		opcodes.put("CMPX indexed", 0xAC); // Indexed
		opcodes.put("CMPX ext", 0xBC); // Extended

		// ========================================
		// CMPY - Compare Y (Page 2)
		// ========================================
		opcodes.put("CMPY #", 0x108C); // Immediate (page 2)
		opcodes.put("CMPY", 0x109C); // Direct
		opcodes.put("CMPY indexed", 0x10AC); // Indexed
		opcodes.put("CMPY ext", 0x10BC); // Extended

		// ========================================
		// CMPU - Compare U (Page 3)
		// ========================================
		opcodes.put("CMPU #", 0x1183); // Immediate (page 3: $11 $83)
		opcodes.put("CMPU", 0x1193); // Direct
		opcodes.put("CMPU indexed", 0x11A3); // Indexed
		opcodes.put("CMPU ext", 0x11B3); // Extended

		// ========================================
		// CMPS - Compare S (Page 3)
		// ========================================
		opcodes.put("CMPS #", 0x118C); // Immediate (page 3: $11 $8C)
		opcodes.put("CMPS", 0x119C); // Direct
		opcodes.put("CMPS indexed", 0x11AC); // Indexed
		opcodes.put("CMPS ext", 0x11BC); // Extended

		// ========================================
		// ANDA - AND with A
		// ========================================
		opcodes.put("ANDA #", 0x84); // Immediate
		opcodes.put("ANDA", 0x94); // Direct
		opcodes.put("ANDA indexed", 0xA4); // Indexed
		opcodes.put("ANDA ext", 0xB4); // Extended

		// ========================================
		// ANDB - AND with B
		// ========================================
		opcodes.put("ANDB #", 0xC4); // Immediate
		opcodes.put("ANDB", 0xD4); // Direct
		opcodes.put("ANDB indexed", 0xE4); // Indexed
		opcodes.put("ANDB ext", 0xF4); // Extended

		// ========================================
		// ANDCC - AND avec registre CC (Code Condition)
		// ========================================
		opcodes.put("ANDCC #", 0x1C); // Immediate

		// ========================================
		// CLR - Clear Memory
		// ========================================
		opcodes.put("CLR", 0x0F); // Direct
		opcodes.put("CLR indexed", 0x6F); // Indexed
		opcodes.put("CLR ext", 0x7F); // Extended

		// ========================================
		// NEG - Negate Memory (modes direct, indexed, extended)
		// ========================================
		opcodes.put("NEG", 0x00); // Direct
		opcodes.put("NEG indexed", 0x60); // Indexed
		opcodes.put("NEG ext", 0x70); // Extended

		// ========================================
		// ORA - OR with A
		// ========================================
		opcodes.put("ORA #", 0x8A); // Immediate
		opcodes.put("ORA", 0x9A); // Direct
		opcodes.put("ORA indexed", 0xAA); // Indexed
		opcodes.put("ORA ext", 0xBA); // Extended

		// ========================================
		// ORB - OR with B
		// ========================================
		opcodes.put("ORB #", 0xCA); // Immediate
		opcodes.put("ORB", 0xDA); // Direct
		opcodes.put("ORB indexed", 0xEA); // Indexed
		opcodes.put("ORB ext", 0xFA); // Extended

		// ========================================
		// ORCC - OR avec registre CC (Code Condition)
		// ========================================
		opcodes.put("ORCC #", 0x1A); // Immediate uniquement

		// ========================================
		// ASR - Arithmetic Shift Right
		// ========================================
		opcodes.put("ASR", 0x07); // Direct
		opcodes.put("ASR indexed", 0x67); // Indexed
		opcodes.put("ASR ext", 0x77); // Extended

		// ========================================
		// ASL - Arithmetic Shift Left
		// ========================================
		opcodes.put("ASL", 0x08); // Direct
		opcodes.put("ASL indexed", 0x68); // Indexed
		opcodes.put("ASL ext", 0x78); // Extended

		// ========================================
		// BITA - Bit Test A
		// ========================================
		opcodes.put("BITA #", 0x85); // Immediate
		opcodes.put("BITA", 0x95); // Direct
		opcodes.put("BITA indexed", 0xA5); // Indexed
		opcodes.put("BITA ext", 0xB5); // Extended

		// ========================================
		// BITB - Bit Test B
		// ========================================
		opcodes.put("BITB #", 0xC5); // Immediate
		opcodes.put("BITB", 0xD5); // Direct
		opcodes.put("BITB indexed", 0xE5); // Indexed
		opcodes.put("BITB ext", 0xF5); // Extended

		// ========================================
		// PSHS - Push registers onto S stack
		// ========================================
		opcodes.put("PSHS", 0x34); // Immediate (mask byte)

		// ========================================
		// PULS - Pull registers from S stack
		// ========================================
		opcodes.put("PULS", 0x35); // Immediate (mask byte)

		// ========================================
		// PSHU - Push registers onto U stack
		// ========================================
		opcodes.put("PSHU", 0x36); // Immediate (mask byte)

		// ========================================
		// PULU - Pull registers from U stack
		// ========================================
		opcodes.put("PULU", 0x37); // Immediate (mask byte)

		// ========================================
		// COM - COMPLEMENT MEMORY
		// ========================================
		opcodes.put("COM", 0x03); // Direct
		opcodes.put("COM indexed", 0x63); // Indexed
		opcodes.put("COM ext", 0x73); // Extended
		// ==============================

		// ========================================
		// CWAI - Clear CC and Wait for Interrupt
		// ========================================
		opcodes.put("CWAI #", 0x3C);

		// ============================================
		// DEC - DECREMENT MEMORY
		// ============================================
		opcodes.put("DEC", 0x0A); // Direct
		opcodes.put("DEC indexed", 0x6A); // Indexed
		opcodes.put("DEC ext", 0x7A); // Extended

		// ========================================
		// EORA - XOR with A
		// ========================================
		opcodes.put("EORA #", 0x88); // Immediate
		opcodes.put("EORA", 0x98); // Direct
		opcodes.put("EORA indexed", 0xA8); // Indexed
		opcodes.put("EORA ext", 0xB8); // Extended

		// ========================================
		// EORB - XOR with B
		// ========================================
		opcodes.put("EORB #", 0xC8); // Immediate
		opcodes.put("EORB", 0xD8); // Direct
		opcodes.put("EORB indexed", 0xE8); // Indexed
		opcodes.put("EORB ext", 0xF8); // Extended

		// ============================================
		// INC - INCREMENT MEMORY
		// ============================================
		opcodes.put("INC", 0x0C); // Direct
		opcodes.put("INC indexed", 0x6C); // Indexed
		opcodes.put("INC ext", 0x7C); // Extended

		// ========================================
		// EXG - Exchange Registers
		// ========================================
		opcodes.put("EXG", 0x1E); // Immediate (postbyte)

		// ========================================
		// LEAX - Load Effective Address into X
		// ========================================
		opcodes.put("LEAX indexed", 0x30); // Indexed uniquement

		// ========================================
		// LEAY - Load Effective Address into Y
		// ========================================
		opcodes.put("LEAY indexed", 0x31); // Indexed uniquement

		// ========================================
		// LEAS - Load Effective Address into S
		// ========================================
		opcodes.put("LEAS indexed", 0x32); // Indexed uniquement

		// ========================================
		// LEAU - Load Effective Address into U
		// ========================================
		opcodes.put("LEAU indexed", 0x33); // Indexed uniquement

		// ========================================
		// TST - Test Memory or Register
		// ========================================
		opcodes.put("TST", 0x0D); // Direct
		opcodes.put("TST indexed", 0x6D); // Indexed
		opcodes.put("TST ext", 0x7D); // Extended

		// ========================================
		// Instructions Inhérentes (sans opérande)
		// ========================================
		opcodes.put("CLRA", 0x4F);
		opcodes.put("CLRB", 0x5F);
		opcodes.put("INCA", 0x4C);
		opcodes.put("INCB", 0x5C);
		opcodes.put("DECA", 0x4A);
		opcodes.put("DECB", 0x5A);
		opcodes.put("MUL", 0x3D);
		opcodes.put("NEGA", 0x40);
		opcodes.put("NEGB", 0x50);
		opcodes.put("COMA", 0x43);
		opcodes.put("COMB", 0x53);
		opcodes.put("ASLA", 0x48);
		opcodes.put("ASLB", 0x58);
		opcodes.put("ASRA", 0x47);
		opcodes.put("ASRB", 0x57);
		opcodes.put("LSLA", 0x48);
		opcodes.put("LSLB", 0x58);
		opcodes.put("LSRA", 0x44);
		opcodes.put("LSRB", 0x54);
		opcodes.put("ROLA", 0x49);
		opcodes.put("ROLB", 0x59);
		opcodes.put("RORA", 0x46);
		opcodes.put("RORB", 0x56);
		opcodes.put("TSTA", 0x4D);
		opcodes.put("TSTB", 0x5D);
		opcodes.put("NOP", 0x12);
		opcodes.put("SWI", 0x3F);
		opcodes.put("RTI", 0x3B);
		opcodes.put("RTS", 0x39);
		opcodes.put("ABX", 0x3A);
		opcodes.put("DAA", 0x19);
		opcodes.put("SEX", 0x1D);
		
		opcodes.put("END", 0x3F);

		// ========================================
		// Branches (8-bit relative)
		// ========================================
		opcodes.put("BRA", 0x20);
		opcodes.put("BRN", 0x21);
		opcodes.put("BHI", 0x22);
		opcodes.put("BLS", 0x23);
		opcodes.put("BCC", 0x24);
		opcodes.put("BCS", 0x25);
		opcodes.put("BNE", 0x26);
		opcodes.put("BEQ", 0x27);
		opcodes.put("BVC", 0x28);
		opcodes.put("BVS", 0x29);
		opcodes.put("BPL", 0x2A);
		opcodes.put("BMI", 0x2B);
		opcodes.put("BGE", 0x2C);
		opcodes.put("BLT", 0x2D);
		opcodes.put("BGT", 0x2E);
		opcodes.put("BLE", 0x2F);

		// ========================================
		// Jumps
		// ========================================
		opcodes.put("JMP indexed", 0x6E);
		opcodes.put("JMP ext", 0x7E);
		opcodes.put("JSR indexed", 0xAD);
		opcodes.put("JSR ext", 0xBD);

		// ========================================
		// SBCA - Subtract with Carry from A
		// ========================================
		opcodes.put("SBCA #", 0x82); // Immediate
		opcodes.put("SBCA", 0x92); // Direct
		opcodes.put("SBCA indexed", 0xA2); // Indexed
		opcodes.put("SBCA ext", 0xB2); // Extended

		// ========================================
		// SBCB - Subtract with Carry from B
		// ========================================
		opcodes.put("SBCB #", 0xC2); // Immediate
		opcodes.put("SBCB", 0xD2); // Direct
		opcodes.put("SBCB indexed", 0xE2); // Indexed
		opcodes.put("SBCB ext", 0xF2); // Extended

		// ========================================
		// ROL - Rotate Left through Carry
		// ========================================
		opcodes.put("ROL", 0x09); // Direct
		opcodes.put("ROL indexed", 0x69); // Indexed
		opcodes.put("ROL ext", 0x79); // Extended

		// ========================================
		// TFR - Transfer Register to Register
		// ========================================
		opcodes.put("TFR", 0x1F); // Immediate (postbyte)

		// ========================================
		// ROR - Rotate Right through Carry
		// ========================================
		opcodes.put("ROR", 0x06); // Direct
		opcodes.put("ROR indexed", 0x66); // Indexed
		opcodes.put("ROR ext", 0x76); // Extended

		// ========================================
		// SWI2 - Software Interrupt 2 (Page 2)
		// ========================================
		opcodes.put("SWI2", 0x103F); // Page 2: $10 $3F

		// ========================================
		// SWI3 - Software Interrupt 3 (Page 3)
		// ========================================
		opcodes.put("SWI3", 0x113F); // Page 3: $11 $3F

		// ========================================
		// SYNC - Synchronize with interrupt
		// ========================================
		opcodes.put("SYNC", 0x13); // Inherent
	}

	public void assembler(String code, Memoire mem, int adresseDepart) throws Exception {
		String[] lignes = code.split("\n");
		int adresse = adresseDepart;

		// Première passe : détecter les labels ET gérer ORG
		labels.clear();
		for (String ligne : lignes) {
			ligne = ligne.trim();
			if (ligne.isEmpty() || ligne.startsWith(";"))
				continue;

			// Gérer la directive ORG
			if (ligne.toUpperCase().startsWith("ORG")) {
				String[] parts = ligne.split("\\s+");
				if (parts.length >= 2) {
					String addrStr = parts[1].trim();
					if (addrStr.startsWith("$")) {
						adresse = Integer.parseInt(addrStr.substring(1), 16);
					} else {
						adresse = Integer.parseInt(addrStr);
					}
					System.out.printf("ORG: Adresse changée à $%04X\n", adresse);
					continue;
				}
			}

			if (ligne.endsWith(":")) {
				String label = ligne.substring(0, ligne.length() - 1);
				labels.put(label, adresse);
				System.out.printf("Label '%s' → $%04X\n", label, adresse);
			} else {
				adresse += estimerTaille(ligne);
			}
		}

		// Deuxième passe : assembler
		adresse = adresseDepart;
		for (String ligne : lignes) {
			ligne = ligne.trim();
			if (ligne.isEmpty() || ligne.startsWith(";") || ligne.endsWith(":"))
				continue;

			// Gérer ORG
			if (ligne.toUpperCase().startsWith("ORG")) {
				String[] parts = ligne.split("\\s+");
				if (parts.length >= 2) {
					String addrStr = parts[1].trim();
					if (addrStr.startsWith("$")) {
						adresse = Integer.parseInt(addrStr.substring(1), 16);
					} else {
						adresse = Integer.parseInt(addrStr);
					}
					continue;
				}
			}

			adresse += assemblerLigne(ligne, mem, adresse);
		}

		System.out.printf("\n✅ Assemblage terminé : %d bytes générés\n", adresse - adresseDepart);
	}

	private int assemblerLigne(String ligne, Memoire mem, int adresse) throws Exception {
		String[] parts = ligne.split("\\s+", 2);
		String instr = parts[0].toUpperCase();
		String operande = parts.length > 1 ? parts[1].trim() : "";

		// ========================================
		// NEG - Traitement spécial (pas de mode immédiat)
		// ========================================
		if (instr.equals("NEG")) {
			if (operande.startsWith("$") && operande.length() > 3) {
				// Extended
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("NEG ext");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) ((value >> 8) & 0xFF));
				mem.forceSetByte(adresse + 2, (byte) (value & 0xFF));
				return 3;
			} else if (operande.startsWith("$")) {
				// Direct
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("NEG");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) value);
				return 2;
			} else if (operande.contains(",")) {
				// Indexed
				Integer opcode = opcodes.get("NEG indexed");
				mem.forceSetByte(adresse, (byte) opcode.intValue());

				if (operande.equals(",X")) {
					mem.forceSetByte(adresse + 1, (byte) 0x84);
					return 2;
				} else if (operande.equals(",Y")) {
					mem.forceSetByte(adresse + 1, (byte) 0xA4);
					return 2;
				}
			}
			throw new Exception("Format d'opérande non reconnu pour NEG: " + operande);
		}

		// ========================================
		// ASL - Traitement spécial (pas de mode immédiat)
		// ========================================
		if (instr.equals("ASL")) {
			if (operande.startsWith("$") && operande.length() > 3) {
				// Extended
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("ASL ext");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) ((value >> 8) & 0xFF));
				mem.forceSetByte(adresse + 2, (byte) (value & 0xFF));
				return 3;
			} else if (operande.startsWith("$")) {
				// Direct
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("ASL");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) value);
				return 2;
			} else if (operande.contains(",")) {
				// Indexed
				Integer opcode = opcodes.get("ASL indexed");
				mem.forceSetByte(adresse, (byte) opcode.intValue());

				if (operande.equals(",X")) {
					mem.forceSetByte(adresse + 1, (byte) 0x84);
					return 2;
				} else if (operande.equals(",Y")) {
					mem.forceSetByte(adresse + 1, (byte) 0xA4);
					return 2;
				}
			}
			throw new Exception("Format d'opérande non reconnu pour ASL: " + operande);
		}

		// ========================================
		// ASR - Traitement spécial (pas de mode immédiat)
		// ========================================
		if (instr.equals("ASR")) {
			if (operande.startsWith("$") && operande.length() > 3) {
				// Extended
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("ASR ext");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) ((value >> 8) & 0xFF));
				mem.forceSetByte(adresse + 2, (byte) (value & 0xFF));
				return 3;
			} else if (operande.startsWith("$")) {
				// Direct
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("ASR");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) value);
				return 2;
			} else if (operande.contains(",")) {
				// Indexed
				Integer opcode = opcodes.get("ASR indexed");
				mem.forceSetByte(adresse, (byte) opcode.intValue());

				if (operande.equals(",X")) {
					mem.forceSetByte(adresse + 1, (byte) 0x84);
					return 2;
				} else if (operande.equals(",Y")) {
					mem.forceSetByte(adresse + 1, (byte) 0xA4);
					return 2;
				}
			}
			throw new Exception("Format d'opérande non reconnu pour ASR: " + operande);
		}

		// ========================================
		// LSR - Traitement spécial (pas de mode immédiat)
		// ========================================
		if (instr.equals("LSR")) {
			if (operande.startsWith("$") && operande.length() > 3) {
				// Extended
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("LSR ext");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) ((value >> 8) & 0xFF));
				mem.forceSetByte(adresse + 2, (byte) (value & 0xFF));
				return 3;
			} else if (operande.startsWith("$")) {
				// Direct
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("LSR");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) value);
				return 2;
			} else if (operande.contains(",")) {
				// Indexed
				Integer opcode = opcodes.get("LSR indexed");
				mem.forceSetByte(adresse, (byte) opcode.intValue());

				if (operande.equals(",X")) {
					mem.forceSetByte(adresse + 1, (byte) 0x84);
					return 2;
				} else if (operande.equals(",Y")) {
					mem.forceSetByte(adresse + 1, (byte) 0xA4);
					return 2;
				}
			}
			throw new Exception("Format d'opérande non reconnu pour LSR: " + operande);
		}

		// ========================================
		// LSL - Traitement spécial (synonyme de ASL)
		// ========================================
		if (instr.equals("LSL")) {
			if (operande.startsWith("$") && operande.length() > 3) {
				// Extended
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("LSL ext");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) ((value >> 8) & 0xFF));
				mem.forceSetByte(adresse + 2, (byte) (value & 0xFF));
				return 3;
			} else if (operande.startsWith("$")) {
				// Direct
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("LSL");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) value);
				return 2;
			} else if (operande.contains(",")) {
				// Indexed
				Integer opcode = opcodes.get("LSL indexed");
				mem.forceSetByte(adresse, (byte) opcode.intValue());

				if (operande.equals(",X")) {
					mem.forceSetByte(adresse + 1, (byte) 0x84);
					return 2;
				} else if (operande.equals(",Y")) {
					mem.forceSetByte(adresse + 1, (byte) 0xA4);
					return 2;
				}
			}
			throw new Exception("Format d'opérande non reconnu pour LSL: " + operande);
		}

		// ========================================
		// LEA* - Load Effective Address (mode indexé uniquement)
		// ========================================
		if (instr.equals("LEAX") || instr.equals("LEAY") || instr.equals("LEAS") || instr.equals("LEAU")) {

			if (!operande.contains(",")) {
				throw new Exception(instr + " nécessite le mode indexé");
			}

			Integer opcode = opcodes.get(instr + " indexed");
			mem.forceSetByte(adresse, (byte) opcode.intValue());

			// Parser le mode indexé
			String[] indexparts = operande.split(",");
			String offset = indexparts[0].trim();
			String reg = indexparts.length > 1 ? indexparts[1].trim() : "";

			// Déterminer le postbyte selon le registre
			byte basePostbyte = 0;
			if (reg.equals("X"))
				basePostbyte = (byte) 0x00;
			else if (reg.equals("Y"))
				basePostbyte = (byte) 0x20;
			else if (reg.equals("U"))
				basePostbyte = (byte) 0x40;
			else if (reg.equals("S"))
				basePostbyte = (byte) 0x60;
			else
				throw new Exception("Registre d'index invalide: " + reg);

			// Cas 1 : ,X (pas d'offset)
			if (offset.isEmpty()) {
				mem.forceSetByte(adresse + 1, (byte) (0x84 | basePostbyte));
				return 2;
			}

			// Cas 2 : Offset numérique
			if (offset.matches("-?\\d+") || offset.startsWith("$")) {
				int offsetVal;
				if (offset.startsWith("$")) {
					offsetVal = Integer.parseInt(offset.substring(1), 16);
				} else {
					offsetVal = Integer.parseInt(offset);
				}

				// Offset 5-bit
				if (offsetVal >= -16 && offsetVal <= 15) {
					byte postbyte = (byte) (basePostbyte | (offsetVal & 0x1F));
					mem.forceSetByte(adresse + 1, postbyte);
					return 2;
				}
				// Offset 8-bit
				else if (offsetVal >= -128 && offsetVal <= 127) {
					mem.forceSetByte(adresse + 1, (byte) (0x88 | basePostbyte));
					mem.forceSetByte(adresse + 2, (byte) offsetVal);
					return 3;
				}
				// Offset 16-bit
				else {
					mem.forceSetByte(adresse + 1, (byte) (0x89 | basePostbyte));
					mem.forceSetByte(adresse + 2, (byte) ((offsetVal >> 8) & 0xFF));
					mem.forceSetByte(adresse + 3, (byte) (offsetVal & 0xFF));
					return 4;
				}
			}

			// Cas 3 : Offset par registre (A,X B,X D,X)
			if (offset.equals("A")) {
				mem.forceSetByte(adresse + 1, (byte) (0x86 | basePostbyte));
				return 2;
			} else if (offset.equals("B")) {
				mem.forceSetByte(adresse + 1, (byte) (0x85 | basePostbyte));
				return 2;
			} else if (offset.equals("D")) {
				mem.forceSetByte(adresse + 1, (byte) (0x8B | basePostbyte));
				return 2;
			}

			throw new Exception("Mode indexé non reconnu pour " + instr + ": " + operande);
		}

		// ========================================
		// EXG - Exchange Registers
		// ========================================
		if (instr.equals("EXG")) {
			Integer opcode = opcodes.get("EXG");
			mem.forceSetByte(adresse, (byte) opcode.intValue());

			// Parser les deux registres (ex: "A,B" ou "X,Y")
			String[] regs = operande.split(",");
			if (regs.length != 2) {
				throw new Exception("EXG nécessite deux registres séparés par une virgule");
			}

			String reg1 = regs[0].trim().toUpperCase();
			String reg2 = regs[1].trim().toUpperCase();

			int code1 = getRegisterCode(reg1);
			int code2 = getRegisterCode(reg2);

			byte postbyte = (byte) ((code1 << 4) | code2);
			mem.forceSetByte(adresse + 1, postbyte);

			return 2;
		}

		// ========================================
		// TFR - Transfer Register to Register
		// ========================================
		if (instr.equals("TFR")) {
			Integer opcode = opcodes.get("TFR");
			mem.forceSetByte(adresse, (byte) opcode.intValue());

			// Parser les deux registres (ex: "A,B" ou "X,Y")
			String[] regs = operande.split(",");
			if (regs.length != 2) {
				throw new Exception("TFR nécessite deux registres séparés par une virgule");
			}

			String reg1 = regs[0].trim().toUpperCase();
			String reg2 = regs[1].trim().toUpperCase();

			int code1 = getRegisterCode(reg1);
			int code2 = getRegisterCode(reg2);

			byte postbyte = (byte) ((code1 << 4) | code2);
			mem.forceSetByte(adresse + 1, postbyte);

			return 2;
		}

		// ========================================
		// ROR - Traitement spécial (pas de mode immédiat)
		// ========================================
		if (instr.equals("ROR")) {
			if (operande.startsWith("$") && operande.length() > 3) {
				// Extended
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("ROR ext");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) ((value >> 8) & 0xFF));
				mem.forceSetByte(adresse + 2, (byte) (value & 0xFF));
				return 3;
			} else if (operande.startsWith("$")) {
				// Direct
				int value = Integer.parseInt(operande.substring(1), 16);
				Integer opcode = opcodes.get("ROR");
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) value);
				return 2;
			} else if (operande.contains(",")) {
				// Indexed
				Integer opcode = opcodes.get("ROR indexed");
				mem.forceSetByte(adresse, (byte) opcode.intValue());

				if (operande.equals(",X")) {
					mem.forceSetByte(adresse + 1, (byte) 0x84);
					return 2;
				} else if (operande.equals(",Y")) {
					mem.forceSetByte(adresse + 1, (byte) 0xA4);
					return 2;
				}
			}
			throw new Exception("Format d'opérande non reconnu pour ROR: " + operande);
		}

		// ========================================
		// TRAITEMENT SPÉCIAL: Instructions PAGE 2 (LDY, STY, etc.)
		// ========================================
		if (instr.equals("LDY") || instr.equals("STY") || instr.equals("LDS") || instr.equals("STS")
				|| instr.equals("CMPD") || instr.equals("CMPY")) {
			// Ces instructions nécessitent un préfixe $10
			mem.forceSetByte(adresse, (byte) 0x10);
			int bytesWritten = 1;

			// Traiter l'opérande
			if (operande.startsWith("#$")) {
				// Immediate 16-bit
				int value = Integer.parseInt(operande.substring(2), 16);
				byte secondOpcode = 0;

				if (instr.equals("LDY"))
					secondOpcode = (byte) 0x8E;
				else if (instr.equals("STY"))
					secondOpcode = (byte) 0x9E;
				else if (instr.equals("LDS"))
					secondOpcode = (byte) 0xCE;
				else if (instr.equals("STS"))
					secondOpcode = (byte) 0xDF;

				mem.forceSetByte(adresse + 1, secondOpcode);
				mem.forceSetByte(adresse + 2, (byte) ((value >> 8) & 0xFF));
				mem.forceSetByte(adresse + 3, (byte) (value & 0xFF));
				return 4;
			} else if (operande.startsWith("$") && operande.length() > 3) {
				// Extended
				int value = Integer.parseInt(operande.substring(1), 16);
				byte secondOpcode = 0;

				if (instr.equals("LDY"))
					secondOpcode = (byte) 0xBE;
				else if (instr.equals("STY"))
					secondOpcode = (byte) 0xBF;
				else if (instr.equals("LDS"))
					secondOpcode = (byte) 0xFE;
				else if (instr.equals("STS"))
					secondOpcode = (byte) 0xFF;

				mem.forceSetByte(adresse + 1, secondOpcode);
				mem.forceSetByte(adresse + 2, (byte) ((value >> 8) & 0xFF));
				mem.forceSetByte(adresse + 3, (byte) (value & 0xFF));
				return 4;
			} else if (operande.startsWith("$")) {
				// Direct
				int value = Integer.parseInt(operande.substring(1), 16);
				byte secondOpcode = 0;

				if (instr.equals("LDY"))
					secondOpcode = (byte) 0x9E;
				else if (instr.equals("STY"))
					secondOpcode = (byte) 0x9F;
				else if (instr.equals("LDS"))
					secondOpcode = (byte) 0xDE;
				else if (instr.equals("STS"))
					secondOpcode = (byte) 0xDF;

				mem.forceSetByte(adresse + 1, secondOpcode);
				mem.forceSetByte(adresse + 2, (byte) value);
				return 3;
			} else if (operande.contains(",")) {
				// Indexed
				byte secondOpcode = 0;

				if (instr.equals("LDY"))
					secondOpcode = (byte) 0xAE;
				else if (instr.equals("STY"))
					secondOpcode = (byte) 0xAF;
				else if (instr.equals("LDS"))
					secondOpcode = (byte) 0xEE;
				else if (instr.equals("STS"))
					secondOpcode = (byte) 0xEF;

				mem.forceSetByte(adresse + 1, secondOpcode);

				if (operande.equals(",X")) {
					mem.forceSetByte(adresse + 2, (byte) 0x84);
					return 3;
				} else if (operande.equals(",Y")) {
					mem.forceSetByte(adresse + 2, (byte) 0xA4);
					return 3;
				}
			}
			// le traitement pour CMPD et CMPY
			if (instr.equals("CMPD") || instr.equals("CMPY")) {
				byte secondOpcode = 0;

				if (operande.startsWith("#$")) {
					// Immediate 16-bit
					int value = Integer.parseInt(operande.substring(2), 16);
					secondOpcode = (byte) (instr.equals("CMPD") ? 0x83 : 0x8C);

					mem.forceSetByte(adresse + 1, secondOpcode);
					mem.forceSetByte(adresse + 2, (byte) ((value >> 8) & 0xFF));
					mem.forceSetByte(adresse + 3, (byte) (value & 0xFF));
					return 4;
				} else if (operande.startsWith("$") && operande.length() > 3) {
					// Extended
					int value = Integer.parseInt(operande.substring(1), 16);
					secondOpcode = (byte) (instr.equals("CMPD") ? 0xB3 : 0xBC);

					mem.forceSetByte(adresse + 1, secondOpcode);
					mem.forceSetByte(adresse + 2, (byte) ((value >> 8) & 0xFF));
					mem.forceSetByte(adresse + 3, (byte) (value & 0xFF));
					return 4;
				} else if (operande.startsWith("$")) {
					// Direct
					int value = Integer.parseInt(operande.substring(1), 16);
					secondOpcode = (byte) (instr.equals("CMPD") ? 0x93 : 0x9C);

					mem.forceSetByte(adresse + 1, secondOpcode);
					mem.forceSetByte(adresse + 2, (byte) value);
					return 3;
				} else if (operande.contains(",")) {
					// Indexed
					secondOpcode = (byte) (instr.equals("CMPD") ? 0xA3 : 0xAC);

					mem.forceSetByte(adresse + 1, secondOpcode);

					if (operande.equals(",X")) {
						mem.forceSetByte(adresse + 2, (byte) 0x84);
						return 3;
					} else if (operande.equals(",Y")) {
						mem.forceSetByte(adresse + 2, (byte) 0xA4);
						return 3;
					}
				}
			}
		}
		// ========================================
		// TRAITEMENT SPÉCIAL: Instructions PAGE 3 ($11 prefix)
		// ========================================
		if (instr.equals("CMPU") || instr.equals("CMPS")) {
			mem.forceSetByte(adresse, (byte) 0x11);

			if (operande.startsWith("#$")) {
				// Immediate 16-bit
				int value = Integer.parseInt(operande.substring(2), 16);
				byte secondOpcode = (byte) (instr.equals("CMPU") ? 0x83 : 0x8C);

				mem.forceSetByte(adresse + 1, secondOpcode);
				mem.forceSetByte(adresse + 2, (byte) ((value >> 8) & 0xFF));
				mem.forceSetByte(adresse + 3, (byte) (value & 0xFF));
				return 4;
			} else if (operande.startsWith("$") && operande.length() > 3) {
				// Extended
				int value = Integer.parseInt(operande.substring(1), 16);
				byte secondOpcode = (byte) (instr.equals("CMPU") ? 0xB3 : 0xBC);

				mem.forceSetByte(adresse + 1, secondOpcode);
				mem.forceSetByte(adresse + 2, (byte) ((value >> 8) & 0xFF));
				mem.forceSetByte(adresse + 3, (byte) (value & 0xFF));
				return 4;
			} else if (operande.startsWith("$")) {
				// Direct
				int value = Integer.parseInt(operande.substring(1), 16);
				byte secondOpcode = (byte) (instr.equals("CMPU") ? 0x93 : 0x9C);

				mem.forceSetByte(adresse + 1, secondOpcode);
				mem.forceSetByte(adresse + 2, (byte) value);
				return 3;
			} else if (operande.contains(",")) {
				// Indexed
				byte secondOpcode = (byte) (instr.equals("CMPU") ? 0xA3 : 0xAC);

				mem.forceSetByte(adresse + 1, secondOpcode);

				if (operande.equals(",X")) {
					mem.forceSetByte(adresse + 2, (byte) 0x84);
					return 3;
				} else if (operande.equals(",Y")) {
					mem.forceSetByte(adresse + 2, (byte) 0xA4);
					return 3;
				}
			}
		}
		// ========================================
		// ANDCC - Cas spécial (immediate uniquement)
		// ========================================
		if (instr.equals("ANDCC")) {
			if (!operande.startsWith("#$") && !operande.startsWith("#")) {
				throw new Exception("ANDCC n'accepte que le mode immédiat");
			}

			int value;
			if (operande.startsWith("#$")) {
				value = Integer.parseInt(operande.substring(2), 16);
			} else {
				value = Integer.parseInt(operande.substring(1));
			}

			Integer opcode = opcodes.get("ANDCC #");
			mem.forceSetByte(adresse, (byte) opcode.intValue());
			mem.forceSetByte(adresse + 1, (byte) value);
			return 2;
		}

		// ========================================
		// ORCC - Cas spécial (immediate uniquement)
		// ========================================
		if (instr.equals("ORCC")) {
			if (!operande.startsWith("#$") && !operande.startsWith("#")) {
				throw new Exception("ORCC n'accepte que le mode immédiat");
			}

			int value;
			if (operande.startsWith("#$")) {
				value = Integer.parseInt(operande.substring(2), 16);
			} else {
				value = Integer.parseInt(operande.substring(1));
			}

			Integer opcode = opcodes.get("ORCC #");
			mem.forceSetByte(adresse, (byte) opcode.intValue());
			mem.forceSetByte(adresse + 1, (byte) value);
			return 2;
		}
		// ========================================
		// PSHS/PULS/PSHU/PULU - Traitement spécial
		// ========================================
		if (instr.equals("PSHS") || instr.equals("PULS") || instr.equals("PSHU") || instr.equals("PULU")) {

			// Parser la liste des registres
			int mask = parseRegisterList(operande);

			Integer opcode = opcodes.get(instr);
			if (opcode == null)
				throw new Exception("Instruction inconnue: " + instr);

			mem.forceSetByte(adresse, (byte) opcode.intValue());
			mem.forceSetByte(adresse + 1, (byte) mask);
			return 2;
		}
		// ========================================
		// Instructions sans opérande
		// ========================================
		if (operande.isEmpty()) {
			Integer opcode = opcodes.get(instr);
			if (opcode == null)
				throw new Exception("Instruction inconnue: " + instr);
			mem.forceSetByte(adresse, (byte) opcode.intValue());
			return 1;
		}

		// ========================================
		// Mode Indexé (,X ,Y etc.)
		// ========================================
		if (operande.contains(",")) {
			Integer opcode = opcodes.get(instr + " indexed");
			if (opcode == null)
				throw new Exception("Instruction inconnue: " + instr + " indexed");

			mem.forceSetByte(adresse, (byte) opcode.intValue());

			// Cas 1 : Auto-incrémentation/décrémentation
			if (operande.equals(",X+")) {
				mem.forceSetByte(adresse + 1, (byte) 0x80);
				return 2;
			} else if (operande.equals(",X++")) {
				mem.forceSetByte(adresse + 1, (byte) 0x81);
				return 2;
			} else if (operande.equals(",-X")) {
				mem.forceSetByte(adresse + 1, (byte) 0x82);
				return 2;
			} else if (operande.equals(",--X")) {
				mem.forceSetByte(adresse + 1, (byte) 0x83);
				return 2;
			}

			// Parse le mode indexé
			String[] indexparts = operande.split(",");
			String offset = indexparts[0].trim();
			String reg = indexparts.length > 1 ? indexparts[1].trim() : "";

			// Déterminer le postbyte selon le registre
			byte basePostbyte = 0;
			if (reg.equals("X"))
				basePostbyte = (byte) 0x00;
			else if (reg.equals("Y"))
				basePostbyte = (byte) 0x20;
			else if (reg.equals("U"))
				basePostbyte = (byte) 0x40;
			else if (reg.equals("S"))
				basePostbyte = (byte) 0x60;
			else
				throw new Exception("Registre d'index invalide: " + reg);

			// Cas 2 : ,X (pas d'offset)
			if (offset.isEmpty()) {
				mem.forceSetByte(adresse + 1, (byte) (0x84 | basePostbyte));
				return 2;
			}

			// Cas 3 : Offset numérique (5,X ou $10,X)
			if (offset.matches("-?\\d+") || offset.startsWith("$")) {
				int offsetVal;
				if (offset.startsWith("$")) {
					offsetVal = Integer.parseInt(offset.substring(1), 16);
				} else {
					offsetVal = Integer.parseInt(offset);
				}

				// Offset 5-bit (-16 à +15)
				if (offsetVal >= -16 && offsetVal <= 15) {
					byte postbyte = (byte) (basePostbyte | (offsetVal & 0x1F));
					mem.forceSetByte(adresse + 1, postbyte);
					return 2;
				}
				// Offset 8-bit (-128 à +127)
				else if (offsetVal >= -128 && offsetVal <= 127) {
					mem.forceSetByte(adresse + 1, (byte) (0x88 | basePostbyte));
					mem.forceSetByte(adresse + 2, (byte) offsetVal);
					return 3;
				}
				// Offset 16-bit
				else {
					mem.forceSetByte(adresse + 1, (byte) (0x89 | basePostbyte));
					mem.forceSetByte(adresse + 2, (byte) ((offsetVal >> 8) & 0xFF));
					mem.forceSetByte(adresse + 3, (byte) (offsetVal & 0xFF));
					return 4;
				}
			}

			// Cas 4 : Offset par registre (A,X B,X D,X)
			if (offset.equals("A")) {
				mem.forceSetByte(adresse + 1, (byte) (0x86 | basePostbyte));
				return 2;
			} else if (offset.equals("B")) {
				mem.forceSetByte(adresse + 1, (byte) (0x85 | basePostbyte));
				return 2;
			} else if (offset.equals("D")) {
				mem.forceSetByte(adresse + 1, (byte) (0x8B | basePostbyte));
				return 2;
			}

			throw new Exception("Mode indexé non reconnu: " + operande);
		}

		// ========================================
		// Mode Immédiat (#)
		// ========================================
		if (operande.startsWith("#$")) {
			int value = Integer.parseInt(operande.substring(2), 16);
			Integer opcode = opcodes.get(instr + " #");
			if (opcode == null)
				throw new Exception("Instruction inconnue: " + instr + " #");

			if (instr.endsWith("D") || instr.endsWith("X") || instr.endsWith("U")) {
				// 16-bit immediate
				if (opcode > 0xFF) {
					mem.forceSetByte(adresse, (byte) ((opcode >> 8) & 0xFF));
					mem.forceSetByte(adresse + 1, (byte) (opcode & 0xFF));
					mem.forceSetByte(adresse + 2, (byte) ((value >> 8) & 0xFF));
					mem.forceSetByte(adresse + 3, (byte) (value & 0xFF));
					return 4;
				} else {
					mem.forceSetByte(adresse, (byte) opcode.intValue());
					mem.forceSetByte(adresse + 1, (byte) ((value >> 8) & 0xFF));
					mem.forceSetByte(adresse + 2, (byte) (value & 0xFF));
					return 3;
				}
			} else {
				// 8-bit immediate
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) value);
				return 2;
			}
		}
		if (operande.startsWith("#")) {
			int value = Integer.parseInt(operande.substring(1));
			Integer opcode = opcodes.get(instr + " #");
			if (opcode == null)
				throw new Exception("Instruction inconnue: " + instr + " #");

			if (instr.endsWith("D") || instr.endsWith("X") || instr.endsWith("U")) {
				// 16-bit immediate
				if (opcode > 0xFF) {
					mem.forceSetByte(adresse, (byte) ((opcode >> 8) & 0xFF));
					mem.forceSetByte(adresse + 1, (byte) (opcode & 0xFF));
					mem.forceSetByte(adresse + 2, (byte) ((value >> 8) & 0xFF));
					mem.forceSetByte(adresse + 3, (byte) (value & 0xFF));
					return 4;
				} else {
					mem.forceSetByte(adresse, (byte) opcode.intValue());
					mem.forceSetByte(adresse + 1, (byte) ((value >> 8) & 0xFF));
					mem.forceSetByte(adresse + 2, (byte) (value & 0xFF));
					return 3;
				}
			} else {
				// 8-bit immediate
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) value);
				return 2;
			}
		}

		// ========================================
		// Mode Extended ($xxxx - 4 chiffres hex)
		// ========================================
		if (operande.startsWith("$") && operande.length() > 3) {
			int value = Integer.parseInt(operande.substring(1), 16);
			Integer opcode = opcodes.get(instr + " ext");
			if (opcode == null)
				throw new Exception("Instruction inconnue: " + instr + " extended");

			mem.forceSetByte(adresse, (byte) opcode.intValue());
			mem.forceSetByte(adresse + 1, (byte) ((value >> 8) & 0xFF));
			mem.forceSetByte(adresse + 2, (byte) (value & 0xFF));
			return 3;
		}

		// ========================================
		// Mode Direct ($xx - 2 chiffres hex)
		// ========================================
		if (operande.startsWith("$")) {
			int value = Integer.parseInt(operande.substring(1), 16);
			Integer opcode = opcodes.get(instr);
			if (opcode == null)
				throw new Exception("Instruction inconnue: " + instr + " direct");

			mem.forceSetByte(adresse, (byte) opcode.intValue());
			mem.forceSetByte(adresse + 1, (byte) value);
			return 2;
		}

		// ========================================
		// JMP - Jump (indexed ou extended uniquement)
		// ========================================
		if (instr.equals("JMP")) {
			if (operande.contains(",")) {
				// Mode indexed
				Integer opcode = opcodes.get("JMP indexed");
				mem.forceSetByte(adresse, (byte) opcode.intValue());

				// Parser le mode indexé
				String[] indexparts = operande.split(",");
				String offset = indexparts[0].trim();
				String reg = indexparts.length > 1 ? indexparts[1].trim() : "";

				byte basePostbyte = 0;
				if (reg.equals("X"))
					basePostbyte = (byte) 0x00;
				else if (reg.equals("Y"))
					basePostbyte = (byte) 0x20;
				else if (reg.equals("U"))
					basePostbyte = (byte) 0x40;
				else if (reg.equals("S"))
					basePostbyte = (byte) 0x60;
				else
					throw new Exception("Registre d'index invalide: " + reg);

				// ,X (pas d'offset)
				if (offset.isEmpty()) {
					mem.forceSetByte(adresse + 1, (byte) (0x84 | basePostbyte));
					return 2;
				}

				// Offset numérique
				if (offset.matches("-?\\d+") || offset.startsWith("$")) {
					int offsetVal;
					if (offset.startsWith("$")) {
						offsetVal = Integer.parseInt(offset.substring(1), 16);
					} else {
						offsetVal = Integer.parseInt(offset);
					}

					if (offsetVal >= -16 && offsetVal <= 15) {
						byte postbyte = (byte) (basePostbyte | (offsetVal & 0x1F));
						mem.forceSetByte(adresse + 1, postbyte);
						return 2;
					} else if (offsetVal >= -128 && offsetVal <= 127) {
						mem.forceSetByte(adresse + 1, (byte) (0x88 | basePostbyte));
						mem.forceSetByte(adresse + 2, (byte) offsetVal);
						return 3;
					} else {
						mem.forceSetByte(adresse + 1, (byte) (0x89 | basePostbyte));
						mem.forceSetByte(adresse + 2, (byte) ((offsetVal >> 8) & 0xFF));
						mem.forceSetByte(adresse + 3, (byte) (offsetVal & 0xFF));
						return 4;
					}
				}

				// Offset par registre
				if (offset.equals("A")) {
					mem.forceSetByte(adresse + 1, (byte) (0x86 | basePostbyte));
					return 2;
				} else if (offset.equals("B")) {
					mem.forceSetByte(adresse + 1, (byte) (0x85 | basePostbyte));
					return 2;
				} else if (offset.equals("D")) {
					mem.forceSetByte(adresse + 1, (byte) (0x8B | basePostbyte));
					return 2;
				}
			} else if (operande.startsWith("$")) {
				// Mode extended
				Integer opcode = opcodes.get("JMP ext");
				int address = Integer.parseInt(operande.substring(1), 16);
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) ((address >> 8) & 0xFF));
				mem.forceSetByte(adresse + 2, (byte) (address & 0xFF));
				return 3;
			} else if (labels.containsKey(operande)) {
				// Mode extended avec label
				Integer opcode = opcodes.get("JMP ext");
				int targetAddr = labels.get(operande);
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) ((targetAddr >> 8) & 0xFF));
				mem.forceSetByte(adresse + 2, (byte) (targetAddr & 0xFF));
				return 3;
			}
			throw new Exception("Format d'opérande non reconnu pour JMP: " + operande);
		}

		// ========================================
		// JSR - Jump to Subroutine (indexed ou extended uniquement)
		// ========================================
		if (instr.equals("JSR")) {
			if (operande.contains(",")) {
				// Mode indexed
				Integer opcode = opcodes.get("JSR indexed");
				mem.forceSetByte(adresse, (byte) opcode.intValue());

				// Parser le mode indexé
				String[] indexparts = operande.split(",");
				String offset = indexparts[0].trim();
				String reg = indexparts.length > 1 ? indexparts[1].trim() : "";

				byte basePostbyte = 0;
				if (reg.equals("X"))
					basePostbyte = (byte) 0x00;
				else if (reg.equals("Y"))
					basePostbyte = (byte) 0x20;
				else if (reg.equals("U"))
					basePostbyte = (byte) 0x40;
				else if (reg.equals("S"))
					basePostbyte = (byte) 0x60;
				else
					throw new Exception("Registre d'index invalide: " + reg);

				// ,X (pas d'offset)
				if (offset.isEmpty()) {
					mem.forceSetByte(adresse + 1, (byte) (0x84 | basePostbyte));
					return 2;
				}

				// Offset numérique
				if (offset.matches("-?\\d+") || offset.startsWith("$")) {
					int offsetVal;
					if (offset.startsWith("$")) {
						offsetVal = Integer.parseInt(offset.substring(1), 16);
					} else {
						offsetVal = Integer.parseInt(offset);
					}

					if (offsetVal >= -16 && offsetVal <= 15) {
						byte postbyte = (byte) (basePostbyte | (offsetVal & 0x1F));
						mem.forceSetByte(adresse + 1, postbyte);
						return 2;
					} else if (offsetVal >= -128 && offsetVal <= 127) {
						mem.forceSetByte(adresse + 1, (byte) (0x88 | basePostbyte));
						mem.forceSetByte(adresse + 2, (byte) offsetVal);
						return 3;
					} else {
						mem.forceSetByte(adresse + 1, (byte) (0x89 | basePostbyte));
						mem.forceSetByte(adresse + 2, (byte) ((offsetVal >> 8) & 0xFF));
						mem.forceSetByte(adresse + 3, (byte) (offsetVal & 0xFF));
						return 4;
					}
				}

				// Offset par registre
				if (offset.equals("A")) {
					mem.forceSetByte(adresse + 1, (byte) (0x86 | basePostbyte));
					return 2;
				} else if (offset.equals("B")) {
					mem.forceSetByte(adresse + 1, (byte) (0x85 | basePostbyte));
					return 2;
				} else if (offset.equals("D")) {
					mem.forceSetByte(adresse + 1, (byte) (0x8B | basePostbyte));
					return 2;
				}
			} else if (operande.startsWith("$")) {
				// Mode extended
				Integer opcode = opcodes.get("JSR ext");
				int address = Integer.parseInt(operande.substring(1), 16);
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) ((address >> 8) & 0xFF));
				mem.forceSetByte(adresse + 2, (byte) (address & 0xFF));
				return 3;
			} else if (labels.containsKey(operande)) {
				// Mode extended avec label
				Integer opcode = opcodes.get("JSR ext");
				int targetAddr = labels.get(operande);
				mem.forceSetByte(adresse, (byte) opcode.intValue());
				mem.forceSetByte(adresse + 1, (byte) ((targetAddr >> 8) & 0xFF));
				mem.forceSetByte(adresse + 2, (byte) (targetAddr & 0xFF));
				return 3;
			}
			throw new Exception("Format d'opérande non reconnu pour JSR: " + operande);
		}

		// ========================================
		// Labels (pour branches)
		// ========================================
		if (labels.containsKey(operande)) {
			Integer opcode = opcodes.get(instr);
			if (opcode == null)
				throw new Exception("Instruction inconnue: " + instr);

			int targetAddr = labels.get(operande);
			int offset = targetAddr - (adresse + 2);

			if (offset < -128 || offset > 127)
				throw new Exception("Branch trop loin: " + offset);

			mem.forceSetByte(adresse, (byte) opcode.intValue());
			mem.forceSetByte(adresse + 1, (byte) offset);
			return 2;
		}

		throw new Exception("Format d'opérande non reconnu: " + operande + " pour " + instr);
	}

	private int estimerTaille(String ligne) {
		String[] parts = ligne.split("\\s+", 2);
		String instr = parts[0].toUpperCase();
		String operande = parts.length > 1 ? parts[1].trim() : "";

		// Instructions PSHS, PULS, PSHU, PULU
		if (instr.equals("PSHS") || instr.equals("PULS") || instr.equals("PSHU") || instr.equals("PULU")) {
			return 2; // Opcode + postbyte
		}

		// Instructions LEA*
		if (instr.equals("LEAX") || instr.equals("LEAY") || instr.equals("LEAS") || instr.equals("LEAU")) {

			if (operande.contains(",")) {
				String[] indexparts = operande.split(",");
				String offset = indexparts[0].trim();

				if (offset.isEmpty())
					return 2;

				if (offset.matches("[ABD]"))
					return 2;

				if (offset.matches("-?\\d+") || offset.startsWith("$")) {
					int val;
					if (offset.startsWith("$")) {
						val = Integer.parseInt(offset.substring(1), 16);
					} else {
						val = Integer.parseInt(offset);
					}

					if (val >= -16 && val <= 15)
						return 2;
					else if (val >= -128 && val <= 127)
						return 3;
					else
						return 4;
				}
			}
			return 2;
		}

		// EXG
		if (instr.equals("EXG"))
			return 2;

		// TFR
		if (instr.equals("TFR"))
			return 2;
		// SYNC
		if (instr.equals("SYNC"))
			return 1;

		// JMP et JSR
		if (instr.equals("JMP") || instr.equals("JSR")) {
			if (operande.contains(",")) {
				String[] indexparts = operande.split(",");
				String offset = indexparts[0].trim();

				if (offset.isEmpty())
					return 2;
				if (offset.matches("[ABD]"))
					return 2;

				if (offset.matches("-?\\d+") || offset.startsWith("$")) {
					int val;
					if (offset.startsWith("$")) {
						val = Integer.parseInt(offset.substring(1), 16);
					} else {
						val = Integer.parseInt(offset);
					}

					if (val >= -16 && val <= 15)
						return 2;
					else if (val >= -128 && val <= 127)
						return 3;
					else
						return 4;
				}
				return 2;
			}
			return 3; // Extended mode
		}

		if (instr.equals("TST")) {
			if (operande.startsWith("$") && operande.length() > 3)
				return 3; // Extended
			if (operande.startsWith("$"))
				return 2; // Direct
			if (operande.contains(","))
				return 2; // Indexed simple
		}
		// ROR
		if (instr.equals("ROR")) {
			if (operande.startsWith("$") && operande.length() > 3)
				return 3; // Extended
			if (operande.startsWith("$"))
				return 2; // Direct
			if (operande.contains(","))
				return 2; // Indexed simple
		}
		// Instructions PAGE 2
		if (instr.equals("LDY") || instr.equals("STY") || instr.equals("LDS") || instr.equals("STS")
				|| instr.equals("CMPD") || instr.equals("CMPY")) {
			if (operande.startsWith("#"))
				return 4; // Préfixe + opcode + 16-bit value
			if (operande.startsWith("$") && operande.length() > 3)
				return 4; // Extended
			if (operande.startsWith("$"))
				return 3; // Direct
			if (operande.contains(","))
				return 3; // Indexed
			if (instr.equals("SWI2"))
				return 2;
			return 4;
		}

		// Instructions PAGE 3 (CMPU, CMPS)
		if (instr.equals("CMPU") || instr.equals("CMPS")) {
			if (operande.startsWith("#"))
				return 4;
			if (operande.startsWith("$") && operande.length() > 3)
				return 4;
			if (operande.startsWith("$"))
				return 3;
			if (operande.contains(","))
				return 3;
			if (instr.equals("SWI3"))
				return 2;
			return 4;
		}
		if (instr.equals("NEG")) {
			if (operande.startsWith("$") && operande.length() > 3)
				return 3; // Extended
			if (operande.startsWith("$"))
				return 2; // Direct
			if (operande.contains(","))
				return 2; // Indexed simple
		}
		if (instr.equals("ASL") || instr.equals("ASR")) {
			if (operande.startsWith("$") && operande.length() > 3)
				return 3; // Extended
			if (operande.startsWith("$"))
				return 2; // Direct
			if (operande.contains(","))
				return 2; // Indexed simple
		}
		if (instr.equals("LSR") || instr.equals("LSL")) {
			if (operande.startsWith("$") && operande.length() > 3)
				return 3; // Extended
			if (operande.startsWith("$"))
				return 2; // Direct
			if (operande.contains(","))
				return 2; // Indexed simple
		}
		if (instr.equals("ROL")) {
			if (operande.startsWith("$") && operande.length() > 3)
				return 3; // Extended
			if (operande.startsWith("$"))
				return 2; // Direct
			if (operande.contains(","))
				return 2; // Indexed simple
		}
		if (operande.isEmpty())
			return 1;
		if (operande.startsWith("#")) {
			if (instr.endsWith("D") || instr.endsWith("X") || instr.endsWith("U")) {
				return 3; // 16-bit immediate
			}
			return 2; // 8-bit immediate
		}
		if (operande.startsWith("$") && operande.length() > 3)
			return 3; // Extended
		if (operande.startsWith("$"))
			return 2; // Direct

		if (operande.contains(",")) {
			// Parser pour estimer
			String[] indexparts = operande.split(",");
			String offset = indexparts[0].trim();

			// ,X simple
			if (offset.isEmpty())
				return 2;

			// Auto-inc/dec
			if (operande.matches(".*[+-]{1,2}[XYUS]"))
				return 2;

			// Registre offset (A,X B,X D,X)
			if (offset.matches("[ABD]"))
				return 2;

			// Offset numérique
			if (offset.matches("-?\\d+") || offset.startsWith("$")) {
				int val;
				if (offset.startsWith("$")) {
					val = Integer.parseInt(offset.substring(1), 16);
				} else {
					val = Integer.parseInt(offset);
				}

				if (val >= -16 && val <= 15)
					return 2; // 5-bit
				else if (val >= -128 && val <= 127)
					return 3; // 8-bit
				else
					return 4; // 16-bit
			}

			return 2;
		}
		return 2;

	}

	private int getRegisterCode(String reg) throws Exception {
		switch (reg) {
		case "D":
			return 0x00;
		case "X":
			return 0x01;
		case "Y":
			return 0x02;
		case "U":
			return 0x03;
		case "S":
			return 0x04;
		case "PC":
			return 0x05;
		case "A":
			return 0x08;
		case "B":
			return 0x09;
		case "CC":
			return 0x0A;
		case "DP":
			return 0x0B;
		default:
			throw new Exception("Registre invalide pour EXG: " + reg);
		}
	}

	private byte parseRegisterList(String registerList) {
		byte postbyte = 0;

		// Supprimer les espaces
		registerList = registerList.replaceAll("\\s+", "");

		// Diviser par virgules
		String[] regs = registerList.split(",");

		for (String reg : regs) {
			reg = reg.toUpperCase().trim();

			switch (reg) {
			case "CC":
				postbyte |= 0x01;
				break;
			case "A":
				postbyte |= 0x02;
				break;
			case "B":
				postbyte |= 0x04;
				break;
			case "DP":
				postbyte |= 0x08;
				break;
			case "X":
				postbyte |= 0x10;
				break;
			case "Y":
				postbyte |= 0x20;
				break;
			case "U":
				postbyte |= 0x40;
				break; // Pour PSHS/PULS
			case "S":
				postbyte |= 0x40;
				break; // Pour PSHU/PULU
			case "PC":
				postbyte |= 0x80;
				break;
			case "D":
				postbyte |= 0x06;
				break; // D = A + B
			default:
				System.err.println("⚠️  Registre inconnu dans liste: " + reg);
				break;
			}
		}

		return postbyte;
	}

}