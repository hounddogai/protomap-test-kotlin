package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedFuelBurnGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedFuelBurnGroupEngine(newRegistry())
        val item = newWeightedFuelBurnGroup("code-92", "label-152")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
