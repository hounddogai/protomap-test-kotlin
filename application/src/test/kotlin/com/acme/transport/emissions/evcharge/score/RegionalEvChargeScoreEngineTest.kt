package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalEvChargeScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalEvChargeScoreEngine(newRegistry())
        val item = newRegionalEvChargeScore("code-267", "name-656")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
