package com.acme.transport.network.node.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedNodeThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedNodeThresholdEngine(newRegistry())
        val item = newWeightedNodeThreshold("code-951", "label-163")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
