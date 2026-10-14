package com.acme.transport.linehaul.relay.relaypolicy

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRelayPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRelayPolicyEngine()
        val item = newFallbackRelayPolicy("code-662", "title-672")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
