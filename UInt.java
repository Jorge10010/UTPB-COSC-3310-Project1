import java.util.Arrays;

public class UInt {
    
    // Array representing the bits of the unsigned integer
    protected boolean[] bits;
    // Number of bits used to represent the unsigned integer
    protected int length;

    public UInt(UInt toClone) {
        this.length = toClone.length;
        this.bits = Arrays.copyOf(toClone.bits, this.length);
    }

    public UInt(int i) {
        length = (int)(Math.ceil(Math.log(i)/Math.log(2.0)) + 1);
        bits = new boolean[length];

        for (int b = length -1; b >= 0; b--) {
            bits[b] = i % 2 == 1;
            i = i >> 1;
        }

        for (boolean bit : bits) {
            System.out.print(bit ? "1" : "0");
        }
        System.out.println();
    }

    public UInt() {
        this.length = 0;
        this.bits = new boolean[0];
    }

    @Override
    public UInt clone() {
        return new UInt(this);
    }

    public static UInt clone(UInt u) {
        return new UInt(u);
    }

    public int toInt() {
        int t = 0;

        for (int i = 0; i < length; i++) {
            t = t + (bits[i] ? 1 : 0);

            t = t << 1;
        }

        return t >> 1;
    }

    public static int toInt(UInt u) {
        return u.toInt();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("0b");

        for (int i = 0; i < length; i++) {
            s.append(bits[i] ? "1" : "0");
        }

        return s.toString();
    }

    public void and(UInt u) {
        for (int i = 0; i < Math.min(this.length, u.length); i++) {
            this.bits[this.length - i - 1] =
                    this.bits[this.length - i - 1] &
                        u.bits[u.length - i - 1];
        }

        if (this.length > u.length) {
            for (int i = u.length; i < this.length; i++) {
                this.bits[this.length - i - 1] = false;
            }
        }
    }

    public static UInt and(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.and(b);
        return temp;
    }

    public void or(UInt u) {
        for (int i = 0; i < Math.min(this.length, u.length); i++) {
            this.bits[this.length - i - 1] |= u.bits[u.length - i - 1];
        }

    }

    public static UInt or(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.or(b);
        return temp;
    }

    public void xor(UInt u) {
        for (int i = 0; i < Math.min(this.length, u.length); i++) {
            this.bits[this.length - i - 1] ^= u.bits[u.length - i - 1];
        }
    }

    public static UInt xor(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.xor(b);
        return temp;
    }

    private static boolean[] addBits(boolean bit1, boolean bit2, boolean carry) {
        boolean sum = (bit1 ^ bit2) ^ carry;
        boolean updatedCarry = (bit1 && bit2) || (bit1 && carry) || (bit2 && carry);

        return new boolean[] {sum, updatedCarry};
    }

    public void add(UInt u) {
        // adjust length of 'this' to match u.length
        int maxLength = Math.max(this.length, u.length);
        if (this.length < maxLength) {
            boolean[] newBits = new boolean[maxLength];
            System.arraycopy(this.bits, 0, newBits, maxLength - this.length, this.length);
            this.bits = newBits;
            this.length = maxLength;
        }

        System.out.println(this.toString());
        System.out.println(u.toString());

        // perform add operation on each individual bit and update carry
        boolean carry = false;
        for (int i = 0; i < this.length; i++) {
            boolean bit1 = this.bits[this.length - i - 1];
            boolean bit2 = i < u.length ? u.bits[u.length - i - 1] : false;

            boolean[] result = addBits(bit1, bit2, carry);
            this.bits[this.length - i - 1] = result[0]; 
            carry = result[1];
        }

        // handles when carry is still left over
        if (carry) {
            this.bits = Arrays.copyOf(this.bits, this.length + 1);
            System.arraycopy(this.bits, 0, this.bits, 1, this.length);
            this.bits[0] = true;
            this.length++;
        }
    }

    public static UInt add(UInt a, UInt b) {
        UInt temp = a.length >= b.length ? a.clone() : b.clone();
        temp.add(a.length >= b.length ? b : a);
        return temp;
    }

    public void negate() {
        // flip all the bits
        for (int i = 0; i < this.length; i++) {
            this.bits[i] = !this.bits[i];
        }

        // add 1 for two's complement negation
        boolean carry = true;
        for (int i = this.length - 1; i >= 0; i--) {
            if (carry) {
                this.bits[i] = !this.bits[i];
                carry = !this.bits[i];
            } else break;
        }

        // handles when carry is still left over
        if (carry) {
            this.bits = Arrays.copyOf(this.bits, this.length + 1);
            this.bits[0] = true;
            this.length++;
        }
    }

    private static int compare(UInt a, UInt b) {
        // returns 0 for equal -1 for smaller and 1 for greater
        if (a.length != b.length) {
            return Integer.compare(a.length, b.length);
        }

        for (int i = 0; i < a.length; i++) {
            if (a.bits[i] != b.bits[i]) {
                return a.bits[i] ? 1 : -1;
            }
        }
        return 0;
    }

    public void sub(UInt u) {
        // If u is larger than 'this' then it will be negative and thus 0
        if (UInt.compare(this, u) < 0) {
            this.bits = new boolean[this.length];
            return;
        }

        int originalLength = this.bits.length;

        // get two's complement
        UInt negateU = u.clone();
        negateU.negate();

        // add two's complement to 'this'
        this.add(negateU);

    }

    public static UInt sub(UInt a, UInt b) {
        // if a is < b then it will be negative or 0
        if (UInt.compare(a, b) < 0) {
            return new UInt();
        }

        UInt temp = a.clone();
        temp.sub(b);
        return temp;
    }

    private void padWithLeadingZero() {
        if (this.bits[0]) {
            boolean[] newBits = new boolean[this.length + 1];
            System.arraycopy(this.bits, 0, newBits, 1, this.length);
            this.bits = newBits;
            this.length++;
        }
    }

    public void mul(UInt u) {
        // Perform padding
        this.padWithLeadingZero();
        u.padWithLeadingZero();

        int maxLength = this.length + u.length;
        UInt product = new UInt();
        product.bits = new boolean[maxLength];
        product.length = maxLength;

        UInt multiplyee = this.clone();
        multiplyee.padWithLeadingZero();
        multiplyee.bits = Arrays.copyOf(multiplyee.bits, maxLength);
        multiplyee.length = maxLength;

        UInt multiplier = u.clone();
        multiplier.padWithLeadingZero();
        multiplier.bits = Arrays.copyOf(multiplier.bits, maxLength);
        multiplier.length = maxLength;

        // Booth's algorithm
        boolean carry = false;
        for (int i = 0; i < u.length; i++) {
            boolean lsb = multiplier.bits[maxLength - 1]; // least significant bit
            boolean carryIn = carry;

            if (lsb && !carryIn) {
                multiplyee.negate();
                product.add(multiplyee);
                multiplyee.negate();
            } else if (!lsb && carryIn) {
                product.add(multiplyee);
            }

            carry = product.bits[0];
            for (int j = 0; j < maxLength - 1; j++) {
                product.bits[j] = product.bits[j + 1];
            }
            
            product.bits[maxLength - 1] = multiplier.bits[0];

            for (int j = 0; j < maxLength - 1; j++) {
                multiplier.bits[j] = multiplier.bits[j + 1];
            }

            multiplier.bits[maxLength - 1] = carry;
        }

        this.bits = Arrays.copyOf(product.bits, maxLength);
        this.length = maxLength;
    }

    public static UInt mul(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.mul(b);
        return temp;
    }

}
