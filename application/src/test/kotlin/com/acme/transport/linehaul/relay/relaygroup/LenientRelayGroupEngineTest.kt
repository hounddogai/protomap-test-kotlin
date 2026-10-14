package com.acme.transport.linehaul.relay.relaygroup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientRelayGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientRelayGroupEngine(newRegistry())
        val item = newLenientRelayGroup("code-606", "title-890")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
