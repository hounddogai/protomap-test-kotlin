package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationSnapshotEngine(newRegistry())
        val item = newSortationSnapshot("code-806", "name-829")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
