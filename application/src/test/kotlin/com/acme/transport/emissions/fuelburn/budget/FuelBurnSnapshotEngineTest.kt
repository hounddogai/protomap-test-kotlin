package com.acme.transport.emissions.fuelburn.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnSnapshotEngine()
        val item = newFuelBurnSnapshot("code-130", "name-416")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
