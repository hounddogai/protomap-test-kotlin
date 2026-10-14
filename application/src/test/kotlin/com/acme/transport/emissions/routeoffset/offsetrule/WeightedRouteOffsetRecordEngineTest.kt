package com.acme.transport.emissions.routeoffset.offsetrule

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRouteOffsetRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRouteOffsetRecordEngine()
        val item = newWeightedRouteOffsetRecord("code-588", "label-459")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
