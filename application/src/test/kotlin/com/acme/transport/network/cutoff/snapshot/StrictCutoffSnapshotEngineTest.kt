package com.acme.transport.network.cutoff.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictCutoffSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictCutoffSnapshotEngine()
        val item = newStrictCutoffSnapshot("code-837", "name-575")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
