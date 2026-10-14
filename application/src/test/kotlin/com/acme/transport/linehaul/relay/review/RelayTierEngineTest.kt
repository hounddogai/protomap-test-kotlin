package com.acme.transport.linehaul.relay.review

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayTierEngine()
        val item = newRelayTier("code-530", "name-487")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
