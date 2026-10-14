package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedFuelBurnForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedFuelBurnForecastEngine(newRegistry())
        val item = newBatchedFuelBurnForecast("code-952", "label-866")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
