package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackFuelBurnMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackFuelBurnMilestoneEngine(newRegistry())
        val item = newFallbackFuelBurnMilestone("code-562", "title-382")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
