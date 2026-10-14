package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTireBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTireBudgetEngine(newRegistry())
        val item = newSeasonalTireBudget("code-396", "name-943")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
