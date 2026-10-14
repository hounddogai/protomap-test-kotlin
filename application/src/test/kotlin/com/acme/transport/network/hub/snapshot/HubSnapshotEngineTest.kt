package com.acme.transport.network.hub.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class HubSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubSnapshotEngine()
        val item = newHubSnapshot("code-322", "title-176")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
