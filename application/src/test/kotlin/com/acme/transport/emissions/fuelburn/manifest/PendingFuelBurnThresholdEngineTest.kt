package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingFuelBurnThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingFuelBurnThresholdEngine(newRegistry())
        val item = newPendingFuelBurnThreshold("code-500", "label-421")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
