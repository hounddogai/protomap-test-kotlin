package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRedeliverySnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRedeliverySnapshotEngine()
        val item = newFallbackRedeliverySnapshot("code-230", "name-354")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
