package com.acme.transport.network.transittime.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeSnapshotEngine()
        val item = newTransitTimeSnapshot("code-87", "title-61")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
