package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedAvailabilityGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedAvailabilityGroupEngine()
        val item = newWeightedAvailabilityGroup("code-996", "name-82")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
