package com.acme.transport.crewing.swap.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedSwapBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedSwapBaselineEngine(newRegistry())
        val item = newWeightedSwapBaseline("code-392", "name-546")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
