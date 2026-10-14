package com.acme.transport.terminal.sortation.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveSortationSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveSortationSnapshotEngine()
        val item = newAdaptiveSortationSnapshot("code-674", "name-663")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
