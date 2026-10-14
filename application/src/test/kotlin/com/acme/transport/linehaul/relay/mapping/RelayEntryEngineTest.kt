package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayEntryEngine()
        val item = newRelayEntry("code-497", "label-955")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
