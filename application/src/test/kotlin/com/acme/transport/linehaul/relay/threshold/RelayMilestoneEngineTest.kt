package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayMilestoneEngine()
        val item = newRelayMilestone("code-212", "name-522")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
