package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacitySummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacitySummaryEngine()
        val item = newCapacitySummary("code-943", "name-274")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
