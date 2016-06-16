package io.pivotal.labs.cfenv;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CloudFoundryServiceTests {

    @Test
    public void shouldRevealAUri() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"uri\": \"http://example.org\"}");

        assertThat(service.getUri(), equalTo(URI.create("http://example.org")));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionOnANonexistentUri() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{}");

        service.getUri();
    }

    @Test(expected = URISyntaxException.class)
    public void shouldNotRevealAMalformedUri() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"uri\": \"http colon slash slash example dot org\"}");

        service.getUri();
    }

    @Test
    public void shouldGetACredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"name\": \"Gurgiunt Brabtruc\"}");

        assertThat(service.getCredential("name"), equalTo("Gurgiunt Brabtruc"));
    }

    @Test
    public void shouldGetANullCredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"name\": null}");

        assertThat(service.getCredential("name"), equalTo(null));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionOnANonexistentCredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{}");

        service.getCredential("name");
    }

    @Test
    public void shouldGetANestedCredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"britain\": {\"king\": {\"name\": \"Gurgiunt Brabtruc\"}}}");

        assertThat(service.getCredential("britain", "king", "name"), equalTo("Gurgiunt Brabtruc"));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionOnANonexistentNestedCredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"britain\": {\"king\": {}}}");

        assertThat(service.getCredential("britain", "king", "name"), equalTo("Gurgiunt Brabtruc"));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionOnAMalformedNestedCredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"britain\": {\"king\": true}}");

        assertThat(service.getCredential("britain", "king", "name"), equalTo("Gurgiunt Brabtruc"));
    }

    @Test
    public void shouldExtractAnX509Certificate() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"ssl\": {\"ca_cert\": \"" +
                "-----BEGIN CERTIFICATE-----\\n" +
                "MIIBxTCCAW+gAwIBAgIJAP6oSMcKL5/RMA0GCSqGSIb3DQEBBQUAMCMxITAfBgNV\\n" +
                "BAsTGElBbUFDZXJ0aWZpY2F0ZUF1dGhvcml0eTAeFw0xNjA2MTYxNTA2MzhaFw0x\\n" +
                "NjA3MTYxNTA2MzhaMCMxITAfBgNVBAsTGElBbUFDZXJ0aWZpY2F0ZUF1dGhvcml0\\n" +
                "eTBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCzDE9WLL7gvS8jKqoEDfg0fo11eV1K\\n" +
                "76aoN7+LakBBJlLEMAgO9G1/dYscb9BsSH3lvKrHP3VqOdNxXkyoEeSzAgMBAAGj\\n" +
                "gYUwgYIwHQYDVR0OBBYEFOpbFIPCq891mF/ara3TbvBFJQ+WMFMGA1UdIwRMMEqA\\n" +
                "FOpbFIPCq891mF/ara3TbvBFJQ+WoSekJTAjMSEwHwYDVQQLExhJQW1BQ2VydGlm\\n" +
                "aWNhdGVBdXRob3JpdHmCCQD+qEjHCi+f0TAMBgNVHRMEBTADAQH/MA0GCSqGSIb3\\n" +
                "DQEBBQUAA0EArVBMPE+epXTh0DnI9cgkf4+qLcgHAS2k8gC3/cfSc3gCG1v15DbN\\n" +
                "xr0CoWh17P9Vb3X1CkOLMhyHYpD3tOpKgw==\\n" +
                "-----END CERTIFICATE-----" +
                "\"}}");

        Certificate certificate = service.getCertificate("ssl", "ca_cert");
        assertThat(((X509Certificate) certificate).getSubjectDN().getName(), equalTo("OU=IAmACertificateAuthority"));
    }

    private CloudFoundryService serviceWithCredentials(String credentials) throws CloudFoundryEnvironmentException {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(TestEnvironment.withVcapServicesContainingService("myservice", credentials));
        return environment.getService("myservice");
    }

}