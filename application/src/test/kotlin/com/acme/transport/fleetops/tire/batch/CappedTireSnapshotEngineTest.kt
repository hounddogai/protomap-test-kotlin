package com.acme.transport.fleetops.tire.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedTireSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedTireSnapshotEngine()
        val item = newCappedTireSnapshot("code-720", "name-812")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
