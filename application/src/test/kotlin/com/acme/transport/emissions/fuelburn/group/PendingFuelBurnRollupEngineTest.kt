package com.acme.transport.emissions.fuelburn.group

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingFuelBurnRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingFuelBurnRollupEngine()
        val item = newPendingFuelBurnRollup("code-276", "label-955")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
