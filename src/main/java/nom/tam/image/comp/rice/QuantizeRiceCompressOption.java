package nom.tam.image.comp.rice;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

import nom.tam.image.comp.quant.QuantizeOption;
import nom.tam.image.comp.rice.par.RiceQuantizCompressParameter;

public class QuantizeRiceCompressOption extends QuantizeOption {

    private RiceCompressOption riceCompressOption = new RiceCompressOption();

    public QuantizeRiceCompressOption() {
        super();
        // circulat dependency, musst be cut.
        this.parameters = new RiceQuantizCompressParameter(this);
    }

    @Override
    public QuantizeRiceCompressOption copy() {
        QuantizeRiceCompressOption copy = (QuantizeRiceCompressOption) super.copy();
        copy.riceCompressOption = this.riceCompressOption.copy();
        return copy;
    }

    public RiceCompressOption getRiceCompressOption() {
        return this.riceCompressOption;
    }

    @Override
    public QuantizeRiceCompressOption setTileHeight(int value) {
        super.setTileHeight(value);
        this.riceCompressOption.setTileHeight(value);
        return this;
    }

    @Override
    public QuantizeRiceCompressOption setTileWidth(int value) {
        super.setTileWidth(value);
        this.riceCompressOption.setTileWidth(value);
        return this;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        T result = super.unwrap(clazz);
        if (result == null) {
            return this.riceCompressOption.unwrap(clazz);
        }
        return result;
    }

}