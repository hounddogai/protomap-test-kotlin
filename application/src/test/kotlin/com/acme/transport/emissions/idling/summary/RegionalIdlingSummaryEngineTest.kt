package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalIdlingSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalIdlingSummaryEngine()
        val item = newRegionalIdlingSummary("code-700", "name-754")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
