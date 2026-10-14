package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayPolicyEngine(newRegistry())
        val item = newRelayPolicy("code-991", "label-473")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
