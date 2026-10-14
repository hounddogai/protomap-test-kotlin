package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalCapacityEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalCapacityEstimateEngine(newRegistry())
        val item = newSeasonalCapacityEstimate("code-492", "name-428")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
