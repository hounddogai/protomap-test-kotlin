package com.acme.transport.linehaul.relay.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayDigestEngine()
        val item = newRelayDigest("code-562", "name-61")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
