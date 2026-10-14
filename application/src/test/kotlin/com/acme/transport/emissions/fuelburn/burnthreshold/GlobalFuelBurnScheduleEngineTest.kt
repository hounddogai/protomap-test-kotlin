package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalFuelBurnScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalFuelBurnScheduleEngine(newRegistry())
        val item = newGlobalFuelBurnSchedule("code-459", "title-84")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
