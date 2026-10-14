package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledFuelBurnCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledFuelBurnCycleEngine(newRegistry())
        val item = newScheduledFuelBurnCycle("code-101", "name-697")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
