package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRestBreakAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRestBreakAllocationEngine(newRegistry())
        val item = newWeightedRestBreakAllocation("code-43", "title-895")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
