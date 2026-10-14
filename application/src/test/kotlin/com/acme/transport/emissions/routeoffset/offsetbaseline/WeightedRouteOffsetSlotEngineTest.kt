package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRouteOffsetSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRouteOffsetSlotEngine(newRegistry())
        val item = newWeightedRouteOffsetSlot("code-195", "label-853")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
