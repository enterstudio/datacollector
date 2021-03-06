/**
 * Copyright 2015 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.datacollector.el;

import com.streamsets.pipeline.api.ElConstant;
import com.streamsets.pipeline.api.ElFunction;
import com.streamsets.pipeline.api.el.ELEval;
import com.streamsets.pipeline.api.el.ELEvalException;
import com.streamsets.pipeline.api.el.ELVars;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestELEvaluator {

  @Test
  public void testNULL() throws ELEvalException {
    ELEval elEval = new ELEvaluator(null);
    ELVars variables = elEval.createVariables();
    Object result = elEval.eval(variables, "${NULL}", Object.class);
    Assert.assertNull(result);
  }

  @Test
  public void testElFunction() throws ELEvalException {
    ELEval elEval = new ELEvaluator("testElFunction", ValidTestEl.class);
    ELVars variables = elEval.createVariables();
    Boolean result = elEval.eval(variables, "${location:city() eq CITY}", Boolean.class);
    Assert.assertTrue(result);
  }

  @Test
  public void testElFunctionMetadata() {
    ELEval elEval = new ELEvaluator("testElFunctionMetadata", ValidTestEl.class);

    Assert.assertEquals(elEval.getConfigName(), "testElFunctionMetadata");

    List<ElFunctionDefinition> elFunctionDefinitions = ((ELEvaluator) elEval).getElFunctionDefinitions();

    boolean found = false;
    for (ElFunctionDefinition def : elFunctionDefinitions) {
      if (def.getName().equals("location:city")) {
        Assert.assertEquals("location:city", def.getName());
        Assert.assertEquals("location", def.getGroup());
        Assert.assertEquals("Returns the Address", def.getDescription());
        Assert.assertEquals("String", def.getReturnType());
        found = true;
      }
    }
    Assert.assertTrue(found);
  }

  @Test(expected = RuntimeException.class)
  public void testNonStaticFunctionEl() {
    new ELEvaluator("testNonStaticFunctionEl", NonStaticFunctionEl.class);
  }

  @Test(expected = RuntimeException.class)
  public void testEmptyNameFunctionEl() {
    new ELEvaluator("testEmptyNameFunctionEl", EmptyNameFunctionEl.class);
  }

  @Test
  public void testElConstant() throws ELEvalException {
    ELEval elEval = new ELEvaluator("testElConstant", ValidTestEl.class);
    ELVars variables = elEval.createVariables();
    Boolean result = elEval.eval(variables, "${CITY eq \"San Francisco\"}", Boolean.class);
    Assert.assertTrue(result);
  }

  @Test
  public void testElConstantMetadata() {
    ELEval elEval = new ELEvaluator("testElConstantMetadata", ValidTestEl.class);
    List<ElConstantDefinition> elConstantDefinitions = ((ELEvaluator) elEval).getElConstantDefinitions();

    ElConstantDefinition constDef = null;
    for (ElConstantDefinition def : elConstantDefinitions) {
      if (def.getName().equals("CITY")) {
        constDef = def;
        break;
      }
    }
    Assert.assertNotNull(constDef);
    Assert.assertEquals("CITY", constDef.getName());
    Assert.assertEquals("Declares the CITY constant to be 'San Francisco'", constDef.getDescription());
    Assert.assertEquals("String", constDef.getReturnType());
  }

  @Test(expected = RuntimeException.class)
  public void testNonStaticConstEl() {
    new ELEvaluator("testNonStaticConstEl", NonStaticConstEl.class);
  }

  @Test(expected = RuntimeException.class)
  public void testEmptyNameConstEl() {
    new ELEvaluator("testEmptyNameConstEl", EmptyNameConstEl.class);
  }

  @Test
  public void testParseEL() throws ELEvalException {
    //valid EL
    ELEvaluator.parseEL("${location:city() eq CITY}");

    //Invalid EL
    try {
      ELEvaluator.parseEL("${location:city() eq }");
      Assert.fail("ELEvalException expected as the EL string is not valid");
    } catch (ELEvalException e) {

    }
  }

  public static class ValidTestEl {

    @ElConstant(name = "CITY", description = "Declares the CITY constant to be 'San Francisco'")
    public static final String CITY = "San Francisco";

    @ElFunction(prefix = "location", name = "city", description = "Returns the Address")
    public static String getCity() {
      return "San Francisco";
    }

  }

  public static class NonStaticConstEl {
    @ElConstant(name = "CITY", description = "Declares the CITY constant to be 'San Francisco'")
    public final String CITY = "San Francisco";
  }

  public static class NonPublicConstEl {
    @ElConstant(name = "CITY", description = "Declares the CITY constant to be 'San Francisco'")
    static final String CITY = "San Francisco";
  }

  public static class EmptyNameConstEl {
    @ElConstant(name = "", description = "Declares the CITY constant to be 'San Francisco'")
    public static final String CITY = "San Francisco";
  }

  public static class NonStaticFunctionEl {
    @ElFunction(prefix = "location", name = "city", description = "Returns the Address")
    public String getCity() {
      return "San Francisco";
    }
  }

  public static class NonPublicFunctionEl {
    @ElFunction(prefix = "location", name = "city", description = "Returns the Address")
    static String getCity() {
      return "San Francisco";
    }
  }

  public static class EmptyNameFunctionEl {
    @ElFunction(prefix = "location", name = "", description = "Returns the Address")
    public static String getCity() {
      return "San Francisco";
    }
  }
}
