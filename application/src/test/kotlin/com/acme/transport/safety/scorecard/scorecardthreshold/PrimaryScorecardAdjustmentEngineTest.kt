package com.acme.transport.safety.scorecard.scorecardthreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryScorecardAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryScorecardAdjustmentEngine()
        val item = newPrimaryScorecardAdjustment("code-414", "label-336")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
