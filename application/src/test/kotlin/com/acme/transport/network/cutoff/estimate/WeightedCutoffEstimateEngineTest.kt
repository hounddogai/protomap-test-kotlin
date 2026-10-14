package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCutoffEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCutoffEstimateEngine()
        val item = newWeightedCutoffEstimate("code-981", "label-340")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
