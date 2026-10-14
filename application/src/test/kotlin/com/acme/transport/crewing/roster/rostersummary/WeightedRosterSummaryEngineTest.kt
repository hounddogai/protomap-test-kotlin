package com.acme.transport.crewing.roster.rostersummary

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRosterSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRosterSummaryEngine()
        val item = newWeightedRosterSummary("code-263", "label-149")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
