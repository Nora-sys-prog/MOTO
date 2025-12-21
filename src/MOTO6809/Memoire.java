package MOTO6809;

import java.util.HashMap;
import java.util.Map;

public class Memoire {
	public enum TypeMemoire {
		RAM, ROM
	}

	private byte[] data = new byte[65536];
	private Map<Integer, TypeMemoire> plages = new HashMap<>();

	public Memoire() {
		// Initialiser toute la ROM avec 0xFF
		for (int addr = 0x8000; addr <= 0xFFFF; addr++) {
			data[addr] = (byte) 0xFF;
		}

		definirPlage(0x0000, 0x7FFF, TypeMemoire.RAM);
		definirPlage(0x8000, 0xFFFF, TypeMemoire.ROM);
	}

	public void definirPlage(int debut, int fin, TypeMemoire type) {
		for (int addr = debut; addr <= fin; addr++) {
			plages.put(addr, type);
		}
	}

	public byte getByte(int addr) {
		return data[addr & 0xFFFF];
	}

	public void setByte(int addr, byte value) {
		int maskedAddr = addr & 0xFFFF;
		TypeMemoire type = plages.getOrDefault(maskedAddr, TypeMemoire.RAM);
		if (type == TypeMemoire.ROM) {
			return; // Ignorer l'écriture en ROM
		}
		data[maskedAddr] = value;
	}

	// Force l'écriture même en ROM (pour l'assembleur)

	public void forceSetByte(int addr, byte value) {
		data[addr & 0xFFFF] = value;
	}

	// Force l'écriture d'un word même en ROM

	public void forceSetWord(int addr, int value) {
		forceSetByte(addr, (byte) ((value >> 8) & 0xFF));
		forceSetByte(addr + 1, (byte) (value & 0xFF));
	}

	public int getWord(int addr) {
		int hi = getByte(addr) & 0xFF;
		int lo = getByte(addr + 1) & 0xFF;
		return (hi << 8) | lo;
	}

	public void chargerROM(int debut, byte[] programme) {
		for (int i = 0; i < programme.length && (debut + i) < 65536; i++) {
			int addr = (debut + i) & 0xFFFF;
			if (plages.getOrDefault(addr, TypeMemoire.RAM) == TypeMemoire.ROM) {
				data[addr] = programme[i];
			}
		}
	}

	public TypeMemoire getType(int addr) {
		return plages.getOrDefault(addr & 0xFFFF, TypeMemoire.RAM);
	}
}