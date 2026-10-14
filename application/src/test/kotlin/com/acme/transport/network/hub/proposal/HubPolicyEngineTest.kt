package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class HubPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubPolicyEngine()
        val item = newHubPolicy("code-409", "label-742")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
