/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * #L%
 */

package nom.tam.util;

import java.util.StringTokenizer;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.LongValueException;

/**
 * A no-frills complex value, mainly just for representing complex numbers in FITS headers.
 * Its a non-mutable object that is created with a real and imaginary parts, which can be
 * retrieved thereafter, and provides string formatting that is suited specifically for
 * representation in FITS headers.
 * 
 * @author Attila Kovacs
 *
 * @since 1.16
 */
public class ComplexValue {

    /** The real and imaginary parts */
    private double re, im;
    
    /** 
     * Instantiates a new complex number value with the specified real and imaginary components.
     * 
     * @param re    the real part
     * @param im    thei maginary part
     */
    public ComplexValue(double re, double im) {
        this.re = re;
        this.im = im;
    }
    
    /**
     * Instantiates a new complex number value from the string repressentation of it in
     * a FITS header value.
     * 
     * @param text      The FITS header value representing the complex number, in brackets
     *                  with the real and imaginary pars separated by a comma. Additional
     *                  spaces may surround the component parts.
     * @throws IllegalArgumentException     
     *                  if the supplied string does not appear to be a FITS standard
     *                  representation of a complex value.
     *                  
     */
    public ComplexValue(String text) throws IllegalArgumentException {        
        // Allow the use of 'D' or 'd' to mark the exponent, instead of the standard 'E' or 'e'...
        text = text.trim().toUpperCase().replace('D', 'E');
        
        boolean hasOpeningBracket = text.charAt(0) == '(';
        boolean hasClosingBracket = text.charAt(text.length() - 1) == ')';
        
        if (!hasOpeningBracket && !hasClosingBracket) {
            // Use just the real value.
            re = Double.parseDouble(text);
            return;
        }
        
        if (!hasClosingBracket) {
            if (!FitsFactory.isAllowHeaderRepairs()) {
                throw new IllegalArgumentException("Unfinished complex value: " + text 
                        + "\n\n --> Try FitsFactory.setAllowHeaderRepair(true).\n");
            }
        }
        
        int end = hasClosingBracket ? text.length() - 1 : text.length();
        StringTokenizer tokens = new StringTokenizer(text.substring(1, end), ",; \t");
        if (tokens.countTokens() != 2) {
            if (!FitsFactory.isAllowHeaderRepairs()) {
                throw new IllegalArgumentException("Invalid complex value: " + text 
                        + "\n\n --> Try FitsFactory.setAllowHeaderRepair(true).\n");
            }
        }
        
        if (tokens.hasMoreTokens()) {
            re = Double.parseDouble(tokens.nextToken());
        }
        if (tokens.hasMoreTokens()) {
            im = Double.parseDouble(tokens.nextToken());
        }
    }
    
    /**
     * Checks if the complex value is finite. That is, if neither the real or imaginary parts
     * are NaN or Infinite.
     * 
     * @return      <code>true</code>if neither the real or imaginary parts are NaN or Infinite.
     *              Otherwise <code>false</code>.
     */
    public final boolean isFinite() {
        return Double.isFinite(re) && Double.isFinite(im);
    }
    
    /**
     * Returns the real part of this complex value.
     * 
     * @return      the real part
     * 
     * @see #im()
     */
    public final double re() {
        return re;
    }
    
    /**
     * Returns the imaginary part of this complex value.
     * 
     * @return      the imaginary part
     * 
     * @see #re()
     */
    public final double im() {
        return im;
    }
    
    @Override
    public String toString() {
        return "(" + re + "," + im + ")";
    }
    
    /**
     * Converts this complex value to its string representation with up to the specified 
     * number of decimal places showing after the leading figure, for both the
     * real and imaginary parts. 
     * 
     * @param decimals  the maximum number of decimal places to show.
     * @return          the string representation with the specified precision, which
     *                  may be used in a FITS header.
     *                  
     * @see FlexFormat
     */
    public String toString(int decimals) {
        FlexFormat f = new FlexFormat().setPrecision(decimals); 
        return "(" + f.format(re) + "," + f.format(im) + ")";
    }
    
    /**
     * Converts this comlex value to its string representation using up to the
     * specified number of characters only. The precision may be reduced as
     * necessary to ensure that the representation fits in the allotted space.
     * 
     * @param maxLength     the maximum length of the returned string representation
     * @return              the string representation, possibly with reduced
     *                      precision to fit into the alotted space.
     * @throws LongValueException
     *                      if the space was too short to fit the value even
     *                      with the minimal (1-digit) precision.
     */
    public String toBoundedString(int maxLength) throws LongValueException {
        if (maxLength < MIN_STRING_LENGTH) {
            throw new LongValueException(maxLength, toString());
        }
        
        int decimals = FlexFormat.DOUBLE_DECIMALS;
        
        String s = toString(decimals);
        while (s.length() > maxLength) {
            // Assume both real and imaginary parts shorten the same amount... 
            decimals -= (s.length() - maxLength + 1) / 2;
            
            if (decimals < 0) {
                throw new LongValueException(maxLength, toString());
            }
            s = toString(decimals);
        }
        
        return s;
    }
   
    /** 
     * The minimum size string needed to represent a complex value with 
     * even just single digits for the real and imaginary parts. 
     */
    private static final int MIN_STRING_LENGTH = 5;     // "(#,#)"
}
