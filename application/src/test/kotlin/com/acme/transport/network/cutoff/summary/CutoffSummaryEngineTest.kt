package com.acme.transport.network.cutoff.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffSummaryEngine()
        val item = newCutoffSummary("code-241", "label-168")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
