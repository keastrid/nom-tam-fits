package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.common.FitsException;
import nom.tam.fits.header.NonStandard;
import nom.tam.fits.util.BlackBoxImages;
import nom.tam.util.BufferedFile;
import nom.tam.util.Cursor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserProvidedTest {

    private boolean longStringsEnabled;

    private boolean useHierarch;

    @Before
    public void before() {
        longStringsEnabled = FitsFactory.isLongStringsEnabled();
        useHierarch = FitsFactory.getUseHierarch();
    }

    @After
    public void after() {
        FitsFactory.setLongStringsEnabled(longStringsEnabled);
        FitsFactory.setUseHierarch(useHierarch);
    }

    @Test
    public void testRewriteableHierarchImageWithLongStrings() throws Exception {
        boolean longStringsEnabled = FitsFactory.isLongStringsEnabled();
        boolean useHierarch = FitsFactory.getUseHierarch();
        try {
            FitsFactory.setUseHierarch(true);
            FitsFactory.setLongStringsEnabled(true);

            String filename = "src/test/resources/nom/tam/image/provided/issue49test.fits";
            Fits fits = new Fits(filename);
            Header headerRewriter = fits.getHDU(0).getHeader();
            // the real test is if this throws an exception, it should not!
            headerRewriter.rewrite();
            fits.close();
        } finally {
            FitsFactory.setLongStringsEnabled(longStringsEnabled);
            FitsFactory.setUseHierarch(useHierarch);

        }
    }

    private static ArrayList<Header> getHeaders(String filename) throws IOException, FitsException {
        ArrayList<Header> list = new ArrayList<Header>();
        try (Fits f = new Fits(filename)) {
            BasicHDU<?> readHDU = f.readHDU();
            while (readHDU != null) {
                f.deleteHDU(0);
                list.add(readHDU.getHeader());
                readHDU = f.readHDU();
            }
        }
        return list;
    }

    @Test
    public void testDoRead() throws FileNotFoundException, Exception {
        FitsFactory.setLongStringsEnabled(true);
        ArrayList<Header> headers = getHeaders(BlackBoxImages.getBlackBoxImage("bad.fits"));
        Assert.assertTrue(headers.get(0).getStringValue("INFO____").endsWith("&"));
        Assert.assertEquals(6, headers.size());
    }

    @Test
    public void testCommentStyleInput() throws FitsException, IOException {
        float[][] data = new float[500][500];
        try (Fits f = new Fits()) {
            BasicHDU<?> hdu = FitsFactory.hduFactory(data);
            hdu.getHeader().insertCommentStyle("        ", "---------------FITS Data Generator---------------");
            hdu.getHeader().insertCommentStyle("        ", "This product is generated by me.");
            hdu.getHeader().addValue("NAXIS3", 1000, "Actual number of energy channels");
            f.addHDU(hdu);
            try (BufferedFile bf = new BufferedFile("target/testCommentStyleInput.fits", "rw")) {
                f.write(bf);
            }
        }
        try (Fits f = new Fits("target/testCommentStyleInput.fits")) {
            Header header = f.readHDU().getHeader();
            int foundComments = 0;
            Cursor<String, HeaderCard> iter = header.iterator();
            while (iter.hasNext()) {
                HeaderCard headerCard = iter.next();
                String comment = headerCard.getComment();
                if (comment != null) {
                    if (comment.contains("This product is generated by me.")) {
                        foundComments++;
                    }
                    if (comment.contains("---------------FITS Data Generator---------------")) {
                        foundComments++;
                    }
                }
            }
            Assert.assertEquals(2, foundComments);
        }
    }

    @Test
    public void testSpecialLongStringCaseWithDuplicateHierarch() throws FitsException, IOException {
        FitsFactory.setUseHierarch(true);
        int hierarchKeys = 0;
        try (Fits f = new Fits(BlackBoxImages.getBlackBoxImage("16913-1.fits"))) {
            Header header = f.readHDU().getHeader();
            Assert.assertEquals("", header.findCard("META_0").getValue());
            Cursor<String, HeaderCard> iter = header.iterator();
            while (iter.hasNext()) {
                HeaderCard headerCard = iter.next();
                if (headerCard.getKey().startsWith(NonStandard.HIERARCH.key())) {
                    hierarchKeys++;
                }
            }
            Assert.assertEquals(10, hierarchKeys);
        }
    }
}
