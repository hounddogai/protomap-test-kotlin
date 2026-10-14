package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedHubDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedHubDraftEngine(newRegistry())
        val item = newCappedHubDraft("code-313", "label-475")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
