package com.acme.transport.emissions.fuelburn.burnrequest

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedFuelBurnCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedFuelBurnCycleEngine()
        val item = newBatchedFuelBurnCycle("code-15", "title-889")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
