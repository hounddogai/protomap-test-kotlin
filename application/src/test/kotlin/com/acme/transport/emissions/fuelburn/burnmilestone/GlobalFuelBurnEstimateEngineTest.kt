package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalFuelBurnEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalFuelBurnEstimateEngine(newRegistry())
        val item = newGlobalFuelBurnEstimate("code-310", "label-945")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
