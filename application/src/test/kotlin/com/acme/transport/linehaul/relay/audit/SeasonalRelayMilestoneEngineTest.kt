package com.acme.transport.linehaul.relay.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRelayMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRelayMilestoneEngine()
        val item = newSeasonalRelayMilestone("code-92", "name-752")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
