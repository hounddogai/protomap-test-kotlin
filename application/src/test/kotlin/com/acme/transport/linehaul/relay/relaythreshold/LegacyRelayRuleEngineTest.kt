package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyRelayRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyRelayRuleEngine()
        val item = newLegacyRelayRule("code-537", "title-711")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
