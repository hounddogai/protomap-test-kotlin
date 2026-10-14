package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnAdjustmentEngine(newRegistry())
        val item = newFuelBurnAdjustment("code-891", "title-703")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
