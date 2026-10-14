package com.acme.transport.safety.violation.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultViolationSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultViolationSegmentEngine()
        val item = newDefaultViolationSegment("code-498", "name-775")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
