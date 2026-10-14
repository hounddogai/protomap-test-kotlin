package com.acme.transport.network.hub.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class HubDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubDraftEngine()
        val item = newHubDraft("code-222", "name-120")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
