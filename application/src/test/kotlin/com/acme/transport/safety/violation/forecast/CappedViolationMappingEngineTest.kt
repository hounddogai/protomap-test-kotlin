package com.acme.transport.safety.violation.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedViolationMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedViolationMappingEngine()
        val item = newCappedViolationMapping("code-902", "title-28")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
