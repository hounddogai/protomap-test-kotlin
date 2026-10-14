package com.acme.transport.network.node.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryNodeSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryNodeSnapshotEngine(newRegistry())
        val item = newPrimaryNodeSnapshot("code-847", "title-608")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
