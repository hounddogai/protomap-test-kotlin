package com.acme.transport.safety.violation.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalViolationProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalViolationProfileEngine()
        val item = newRegionalViolationProfile("code-389", "name-67")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
