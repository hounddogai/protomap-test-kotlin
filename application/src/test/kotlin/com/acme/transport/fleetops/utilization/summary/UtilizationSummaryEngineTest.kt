package com.acme.transport.fleetops.utilization.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationSummaryEngine()
        val item = newUtilizationSummary("code-601", "label-287")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
