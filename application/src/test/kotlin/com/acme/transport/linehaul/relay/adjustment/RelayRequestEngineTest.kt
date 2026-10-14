package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayRequestEngine(newRegistry())
        val item = newRelayRequest("code-128", "title-70")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
