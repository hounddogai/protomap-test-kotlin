package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedBackhaulThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedBackhaulThresholdEngine(newRegistry())
        val item = newWeightedBackhaulThreshold("code-763", "name-764")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
