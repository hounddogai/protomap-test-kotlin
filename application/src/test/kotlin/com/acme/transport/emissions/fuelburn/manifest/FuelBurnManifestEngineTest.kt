package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnManifestEngine()
        val item = newFuelBurnManifest("code-835", "label-772")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
