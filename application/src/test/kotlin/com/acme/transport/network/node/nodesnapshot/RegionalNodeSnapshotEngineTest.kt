package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalNodeSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalNodeSnapshotEngine(newRegistry())
        val item = newRegionalNodeSnapshot("code-900", "name-302")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
