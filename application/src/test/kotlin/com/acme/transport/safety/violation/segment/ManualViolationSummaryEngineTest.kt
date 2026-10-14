package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualViolationSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualViolationSummaryEngine()
        val item = newManualViolationSummary("code-76", "name-117")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
