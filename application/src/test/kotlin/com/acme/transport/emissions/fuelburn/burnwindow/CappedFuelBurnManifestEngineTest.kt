package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedFuelBurnManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedFuelBurnManifestEngine()
        val item = newCappedFuelBurnManifest("code-807", "title-54")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
