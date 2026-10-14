package com.acme.transport.network.hub.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class HubManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubManifestEngine()
        val item = newHubManifest("code-270", "label-333")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
