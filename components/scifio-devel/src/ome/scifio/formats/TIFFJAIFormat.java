/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package ome.scifio.formats;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.imglib2.meta.Axes;
import ome.scifio.AbstractFormat;
import ome.scifio.AbstractMetadata;
import ome.scifio.AbstractParser;
import ome.scifio.BufferedImagePlane;
import ome.scifio.FormatException;
import ome.scifio.ImageMetadata;
import ome.scifio.MissingLibraryException;
import ome.scifio.common.ReflectException;
import ome.scifio.common.ReflectedUniverse;
import ome.scifio.gui.AWTImageTools;
import ome.scifio.gui.BufferedImageReader;
import ome.scifio.io.FileHandle;
import ome.scifio.io.IRandomAccess;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;

import org.scijava.plugin.Plugin;

/**
 * TiffJAIReader is a file format reader for TIFF images. It uses the
 * Java Advanced Imaging library (javax.media.jai) to read the data.
 *
 * Much of this code was adapted from
 * <a href="http://java.sun.com/products/java-media/jai/forDevelopers/samples/MultiPageRead.java">this example</a>.
 *
 */
@Plugin(type = TIFFJAIFormat.class, priority = TIFFFormat.PRIORITY - 1)
public class TIFFJAIFormat extends AbstractFormat{

  // -- Format API methods --
  
  /*
   * @see ome.scifio.Format#getFormatName()
   */
  public String getFormatName() {
    return "Tagged Image File Format";
  }

  /*
   * @see ome.scifio.Format#getSuffixes()
   */
  public String[] getSuffixes() {
    return scifio().format().getFormatFromClass(TIFFFormat.class).getSuffixes();
  }
  
  // -- Nested classes --
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Metadata extends AbstractMetadata {

    // -- Fields --

    /** Reflection tool for JAI calls. */
    private ReflectedUniverse r;
    
    private int numPages;
    
    // -- Constants --
    
    public static final String CNAME = "ome.scifio.formats.TIFFJAIFormat$Metadata";
    
    // -- TIFFJAIMetadata getters and setters --

    public ReflectedUniverse universe() {
      return r;
    }

    public void setUniverse(ReflectedUniverse r) {
      this.r = r;
    }
    
    public int getNumPages() {
      return numPages;
    }

    public void setNumPages(int numPages) {
      this.numPages = numPages;
    }

    // -- Metadata API Methods --

    public void populateImageMetadata() {
      createImageMetadata(1);
      ImageMetadata m = get(0);

      // decode first image plane
      BufferedImage img = null;
      try {
        img = openBufferedImage(this, 0);
      } catch (FormatException e) {
        LOGGER.error("Invalid image stream", e);
        return;
      }

      m.setPlaneCount(numPages);

      m.setAxisLength(Axes.X, img.getWidth());
      m.setAxisLength(Axes.Y, img.getHeight());
      m.setAxisLength(Axes.CHANNEL, img.getSampleModel().getNumBands());
      m.setAxisLength(Axes.Z, 1);
      m.setAxisLength(Axes.TIME, numPages);
      
      m.setRGB(m.getAxisLength(Axes.CHANNEL) > 1);
      
      m.setPixelType(AWTImageTools.getPixelType(img));
      m.setBitsPerPixel(FormatTools.getBitsPerPixel(m.getPixelType()));
      m.setInterleaved(true);
      m.setLittleEndian(false);
      m.setMetadataComplete(true);
      m.setIndexed(false);
      m.setFalseColor(false);
    }
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Parser extends AbstractParser<Metadata> {

    // -- Constants --

    private static final String NO_JAI_MSG =
      "Java Advanced Imaging (JAI) is required to read some TIFF files. " +
      "Please install JAI from https://jai.dev.java.net/";

    // -- Parser API Methods --
    
    @Override
    protected void typedParse(RandomAccessInputStream stream, Metadata meta)
      throws IOException, FormatException {
      LOGGER.info("Checking for JAI");
      ReflectedUniverse r = null;
      
      try {
        r = new ReflectedUniverse();
        r.exec("import javax.media.jai.NullOpImage");
        r.exec("import javax.media.jai.OpImage");
        r.exec("import com.sun.media.jai.codec.FileSeekableStream");
        r.exec("import com.sun.media.jai.codec.ImageDecoder");
        r.exec("import com.sun.media.jai.codec.ImageCodec");
      }
      catch (ReflectException exc) {
        throw new MissingLibraryException(NO_JAI_MSG, exc);
      }
      
      meta.setUniverse(r);
      
      String id = stream.getFileName();

      LOGGER.info("Reading movie dimensions");

      // map Location to File or RandomAccessFile, if possible
      IRandomAccess ira = scifio().location().getMappedFile(id);
      if (ira != null) {
        if (ira instanceof FileHandle) {
          FileHandle fh = (FileHandle) ira;
          r.setVar("file", fh.getRandomAccessFile());
        }
        else {
          throw new FormatException(
            "Unsupported handle type" + ira.getClass().getName());
        }
      }
      else {
        String mapId = scifio().location().getMappedId(id);
        File file = new File(mapId);
        if (file.exists()) {
          r.setVar("file", file);
        }
        else throw new FileNotFoundException(id);
      }
      r.setVar("tiff", "tiff");
      r.setVar("param", null);

      // create TIFF decoder
      int numPages;
      try {
        r.exec("s = new FileSeekableStream(file)");
        r.exec("dec = ImageCodec.createImageDecoder(tiff, s, param)");
        numPages = ((Integer) r.exec("dec.getNumPages()")).intValue();
      }
      catch (ReflectException exc) {
        throw new FormatException(exc);
      }
      if (numPages < 0) {
        throw new FormatException("Invalid page count: " + numPages);
      }
    }
    
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Reader extends BufferedImageReader<Metadata> {
    
    // -- Constructor --
    
    public Reader() {
      domains = new String[] {FormatTools.GRAPHICS_DOMAIN};
    }
    
    // -- Reader API methods --
    
    /*
     * @see ome.scifio.TypedReader#openPlane(int, int, ome.scifio.DataPlane, int, int, int, int)
     */
    public BufferedImagePlane openPlane(int imageIndex, int planeIndex,
      BufferedImagePlane plane, int x, int y, int w, int h)
      throws FormatException, IOException {
      FormatTools.checkPlaneParameters(this, imageIndex, planeIndex, -1, x, y, w, h);
      BufferedImage img = openBufferedImage(getMetadata(), planeIndex);
      plane.setData(AWTImageTools.getSubimage(img, getMetadata().isLittleEndian(imageIndex), x, y, w, h));
      return plane;
    }
  }
  
  // -- Helper methods --

  /** Obtains a BufferedImage from the given data source using JAI. */
  protected static BufferedImage openBufferedImage(Metadata meta, int planeIndex) throws FormatException {
    meta.universe().setVar("planeIndex", planeIndex);
    RenderedImage img;
    try {
      meta.universe().exec("img = dec.decodeAsRenderedImage(planeIndex)");
      img = (RenderedImage)
          meta.universe().exec("new NullOpImage(img, null, OpImage.OP_IO_BOUND, null)");
    }
    catch (ReflectException exc) {
      throw new FormatException(exc);
    }
    return AWTImageTools.convertRenderedImage(img);
  }
}