package com.acme.transport.network.cutoff.scheduledcutoffclaim

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCutoffTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCutoffTierEngine()
        val item = newRegionalCutoffTier("code-921", "title-801")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
