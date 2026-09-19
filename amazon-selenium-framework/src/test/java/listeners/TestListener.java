package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Simple TestNG listener providing consistent, centralised logging of
 * suite/test lifecycle events. Registered via testng.xml <listeners>.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        log.info(">>> Suite started: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("<<< Suite finished: {} | Passed={} Failed={} Skipped={}",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
                context.getSkippedTests().size());
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info("--- Test started: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("--- Test PASSED: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("--- Test FAILED: {}", result.getMethod().getMethodName(), result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("--- Test SKIPPED: {}", result.getMethod().getMethodName());
    }
}
