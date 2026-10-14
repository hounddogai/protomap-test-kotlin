package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryScorecardThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryScorecardThresholdEngine()
        val item = newPrimaryScorecardThreshold("code-39", "title-163")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
