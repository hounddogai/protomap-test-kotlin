package com.acme.transport.linehaul.relay.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayDraftEngine(newRegistry())
        val item = newRelayDraft("code-460", "title-763")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
