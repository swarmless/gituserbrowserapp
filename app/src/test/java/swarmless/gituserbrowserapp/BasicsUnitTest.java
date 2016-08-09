package swarmless.gituserbrowserapp;

import org.apache.commons.validator.routines.UrlValidator;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class BasicsUnitTest {
    @Test
    public void endpoint_isValid() throws Exception {

       UrlValidator urlValidator = new UrlValidator();
        assertThat(urlValidator.isValid(App.Const.API_BASE_URL), is(true));

    }

    
}