package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRosterTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRosterTierEngine(newRegistry())
        val item = newWeightedRosterTier("code-143", "title-778")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
