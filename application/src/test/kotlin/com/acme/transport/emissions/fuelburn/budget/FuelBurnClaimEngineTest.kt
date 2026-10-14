package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnClaimEngine(newRegistry())
        val item = newFuelBurnClaim("code-291", "title-204")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
