package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTrailerThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTrailerThresholdEngine(newRegistry())
        val item = newWeightedTrailerThreshold("code-434", "name-758")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
