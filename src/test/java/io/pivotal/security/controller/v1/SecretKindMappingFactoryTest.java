package io.pivotal.security.controller.v1;

import com.greghaskins.spectrum.Spectrum;
import static com.greghaskins.spectrum.Spectrum.beforeEach;
import static com.greghaskins.spectrum.Spectrum.describe;
import static com.greghaskins.spectrum.Spectrum.it;
import com.jayway.jsonpath.DocumentContext;
import io.pivotal.security.domain.Encryptor;
import io.pivotal.security.domain.NamedPasswordSecret;
import static io.pivotal.security.helper.SpectrumHelper.injectMocks;
import io.pivotal.security.mapper.RequestTranslator;
import static org.hamcrest.Matchers.sameInstance;
import org.junit.Assert;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;

@RunWith(Spectrum.class)
public class SecretKindMappingFactoryTest {

  @Mock
  private RequestTranslator<NamedPasswordSecret> requestTranslator;

  @Mock
  private DocumentContext parsedRequest;

  @Mock
  Encryptor encryptor;

  {
    SecretKindMappingFactory subject = (secretPath, parsed) -> null;

    beforeEach(injectMocks(this));

    describe("when there is no existing entity", () -> {
      it("creates a new object", () -> {
        Function<String, NamedPasswordSecret> constructor = mock(Function.class);
        NamedPasswordSecret constructedObject = new NamedPasswordSecret("name");
        when(constructor.apply("name")).thenReturn(constructedObject);

        Assert.assertThat(subject.createNewSecret(null, constructor, "name", requestTranslator, parsedRequest, encryptor, false), sameInstance(constructedObject));
        verify(constructor).apply("name");

        verify(requestTranslator).populateEntityFromJson(constructedObject, parsedRequest);
      });
    });

    describe("when there is an existing entity", () -> {
      it("should create a copy of the original", () -> {
        NamedPasswordSecret existingObject = spy(new NamedPasswordSecret("name"));

        Function<String, NamedPasswordSecret> constructor = mock(Function.class);
        NamedPasswordSecret constructedObject = new NamedPasswordSecret("name");
        when(constructor.apply("name")).thenReturn(constructedObject);

        Assert.assertThat(subject.createNewSecret(existingObject, constructor, "name", requestTranslator, parsedRequest, encryptor, true), sameInstance(constructedObject));
        verify(constructor).apply("name");

        verify(existingObject).copyInto(constructedObject);
        verify(requestTranslator).populateEntityFromJson(constructedObject, parsedRequest);
      });
    });

    describe("validation", () -> {
      it("calls the request translator to validate JSON keys", () -> {
        subject.createNewSecret(null, NamedPasswordSecret::new, "name", requestTranslator, parsedRequest, encryptor, false);
        verify(requestTranslator).validateJsonKeys(parsedRequest);
      });

      it("calls the request translator to validate path", () -> {
        subject.createNewSecret(null, NamedPasswordSecret::new, "/dont//do//this/", requestTranslator, parsedRequest, encryptor, false);
        verify(requestTranslator).validatePathName(any(String.class));
      });
    });
  }
}
