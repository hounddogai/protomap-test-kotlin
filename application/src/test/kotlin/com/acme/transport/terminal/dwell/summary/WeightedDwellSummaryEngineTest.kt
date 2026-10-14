package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedDwellSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedDwellSummaryEngine(newRegistry())
        val item = newWeightedDwellSummary("code-696", "label-219")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
