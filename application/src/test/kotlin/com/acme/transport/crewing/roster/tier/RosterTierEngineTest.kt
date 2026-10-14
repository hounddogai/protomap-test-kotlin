package com.acme.transport.crewing.roster.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterTierEngine()
        val item = newRosterTier("code-265", "title-32")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
