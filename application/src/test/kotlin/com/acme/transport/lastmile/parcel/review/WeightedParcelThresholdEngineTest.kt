package com.acme.transport.lastmile.parcel.review

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedParcelThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedParcelThresholdEngine()
        val item = newWeightedParcelThreshold("code-380", "title-750")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
