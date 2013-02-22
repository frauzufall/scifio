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

package ome.scifio;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * 
 * This class represents a contextual environment of SCIFIO components.
 * <p>
 * The context is the entry point for image IO operations. How this class
 * is constructed determines which formats will be supported in this context.
 * </p>
 * <p>
 * Many of the image IO steps can be abstracted by using the {@link #initializeReader}
 * and {@link #initializeWriter} methods. Alternately, to use a specific {@link ome.scifio.Format}
 * implementation, the {@link #getFormatFromClass(Class)} method can provide a 
 * typed {@code Format} object.
 * </p>
 * <p>
 * Note that all {@code Formats} are singletons in a given context.
 * </p>
 * 
 * @author Mark Hiner
 */
@Plugin(type=Service.class)
public class DefaultSCIFIO extends AbstractService implements SCIFIO {
  
  // -- Parameters --
  
  @Parameter
  private InitializeService initializeService;
  
  @Parameter
  private FormatService formatService;
  
  @Parameter
  private TranslatorService translatorService;
  
  // -- SCIFIO API Methods --

  public InitializeService initializer() {
    return initializeService;
  }

  public FormatService formats() {
    return formatService;
  }
  
  public TranslatorService translators() {
    return translatorService;
  }
}
