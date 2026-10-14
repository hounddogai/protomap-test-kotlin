package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedParcelTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedParcelTargetEngine()
        val item = newWeightedParcelTarget("code-973", "name-740")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
