package MOTO6809;

public class Registres {
	private int A;
	private int B;
	private int X;
	private int Y;
	private int U;
	private int S;
	private int PC;
	private int DP;
	private int CC;

	public static final int CC_E = 0x80;
	public static final int CC_F = 0x40;
	public static final int CC_H = 0x20;
	public static final int CC_I = 0x10;
	public static final int CC_N = 0x08;
	public static final int CC_Z = 0x04;
	public static final int CC_V = 0x02;
	public static final int CC_C = 0x01;

	public Registres() {
		reset();
	}

	public void reset() {
		A = B = 0;
		X = Y = U = 0;
		DP = 0;
		CC = 0x50;// Flags I et F sont SET après reset (interruptions masquées)
		S = 0x7FFF;// Le Stack Pointer est initialisé en haut de la RAM
		U = 0x7FFE; // ← Pile U juste en dessous de S
	}

	public int getA() {
		return A & 0xFF;
	}

	public void setA(int value) {
		A = value & 0xFF;
	}

	public int getB() {
		return B & 0xFF;
	}

	public void setB(int value) {
		B = value & 0xFF;
	}

	public int getD() {
		return ((A & 0xFF) << 8) | (B & 0xFF);
	}

	public void setD(int value) {
		A = (value >> 8) & 0xFF;
		B = value & 0xFF;
	}

	public int getDP() {
		return DP & 0xFF;
	}

	public void setDP(int value) {
		DP = value & 0xFF;
	}

	public int getCC() {
		return CC & 0xFF;
	}

	public void setCC(int value) {
		CC = value & 0xFF;
	}

	public int getX() {
		return X & 0xFFFF;
	}

	public void setX(int value) {
		X = value & 0xFFFF;
	}

	public int getY() {
		return Y & 0xFFFF;
	}

	public void setY(int value) {
		Y = value & 0xFFFF;
	}

	public int getU() {
		return U & 0xFFFF;
	}

	public void setU(int value) {
		U = value & 0xFFFF;
	}

	public int getS() {
		return S & 0xFFFF;
	}

	public void setS(int value) {
		S = value & 0xFFFF;
	}

	public int getPC() {
		return PC & 0xFFFF;
	}

	public void setPC(int value) {
		PC = value & 0xFFFF;
	}

	public void incPC(int increment) {
		PC = (PC + increment) & 0xFFFF;
	}

	public boolean getFlag(int flagMask) {
		return (CC & flagMask) != 0;
	}

	public void setFlag(int flagMask, boolean state) {
		if (state) {
			CC |= flagMask;
		} else {
			CC &= ~flagMask;
		}
	}

	public boolean isNegative() {
		return getFlag(CC_N);
	}

	public boolean isZero() {
		return getFlag(CC_Z);
	}

	public boolean isCarry() {
		return getFlag(CC_C);
	}

	public boolean isOverflow() {
		return getFlag(CC_V);
	}

	public boolean isHalfCarry() {
		return getFlag(CC_H);
	}

	public boolean isIRQMasked() {
		return getFlag(CC_I);
	}

	public boolean isFIRQMasked() {
		return getFlag(CC_F);
	}

	@Override
	public String toString() {
		return String.format(
				"A=%02X B=%02X D=%04X X=%04X Y=%04X U=%04X S=%04X PC=%04X DP=%02X CC=%02X [%s%s%s%s%s%s%s%s]", getA(),
				getB(), getD(), getX(), getY(), getU(), getS(), getPC(), getDP(), getCC(), getFlag(CC_E) ? "E" : "e",
				getFlag(CC_F) ? "F" : "f", getFlag(CC_H) ? "H" : "h", getFlag(CC_I) ? "I" : "i",
				getFlag(CC_N) ? "N" : "n", getFlag(CC_Z) ? "Z" : "z", getFlag(CC_V) ? "V" : "v",
				getFlag(CC_C) ? "C" : "c");
	}
}