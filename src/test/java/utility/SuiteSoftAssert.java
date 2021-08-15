package utility;

import java.util.Map;

import org.testng.asserts.Assertion;
import org.testng.asserts.IAssert;
import org.testng.collections.Maps;

/**
 * When an assertion fails, don't throw an exception but record the failure.
 * Calling {@code assertAll()} will cause an exception to be thrown if at
 * least one assertion failed.
 */
public class SuiteSoftAssert  extends Assertion {
    // LinkedHashMap to preserve the order
    private  Map<AssertionError, IAssert<?>> m_errors = Maps.newLinkedHashMap();

    private boolean latestCheck = false;

    private String latestErrorMessage;

    // Retrieves the latestCheck field
    public boolean getLatestCheck() {
        return this.latestCheck;
    }

    // Sets the latestCheck field
    public void setLatestCheck(boolean status) {
        this.latestCheck = status;
    }

    // Retrieves the latestErrorMessage field
    public String getLatestErrorMessage() {
        return this.latestErrorMessage;
    }

    @Override
    protected void doAssert(IAssert<?> a) {
        onBeforeAssert(a);
        try {
            a.doAssert();
            onAssertSuccess(a);
            latestCheck = true;
        } catch (AssertionError ex) {
            onAssertFailure(a, ex);
            m_errors.put(ex, a);
            latestCheck = false;
            latestErrorMessage = ex.getMessage();
        } finally {
            onAfterAssert(a);
        }
    }

    public void assertAll() {
        if (!m_errors.isEmpty()) {
            StringBuilder sb = new StringBuilder((m_errors.size() > 1 ? (m_errors.size() + " verifications are") : (m_errors.size() + " verification is")) + " found failed for this test case:");
            boolean first = true;
            for (Map.Entry<AssertionError, IAssert<?>> ae : m_errors.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append("\n");
                sb.append(ae.getKey().getMessage());
            }
            m_errors = Maps.newLinkedHashMap();
            throw new AssertionError(sb.toString());
        }
    }
}