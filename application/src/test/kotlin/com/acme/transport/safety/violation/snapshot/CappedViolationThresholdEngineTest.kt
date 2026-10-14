package com.acme.transport.safety.violation.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedViolationThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedViolationThresholdEngine()
        val item = newCappedViolationThreshold("code-495", "title-761")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
