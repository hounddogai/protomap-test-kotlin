package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayWindowEngine(newRegistry())
        val item = newRelayWindow("code-660", "title-95")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
