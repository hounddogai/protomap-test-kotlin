package com.acme.transport.emissions.fuelburn.burnestimate

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedFuelBurnMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedFuelBurnMilestoneEngine()
        val item = newCappedFuelBurnMilestone("code-249", "title-598")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
