package com.acme.transport.linehaul.relay.relayquota

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingRelayRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingRelayRequestEngine()
        val item = newRollingRelayRequest("code-10", "label-291")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
