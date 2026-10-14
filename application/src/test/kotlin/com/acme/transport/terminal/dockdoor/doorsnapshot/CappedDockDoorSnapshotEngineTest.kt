package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDockDoorSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDockDoorSnapshotEngine(newRegistry())
        val item = newCappedDockDoorSnapshot("code-398", "label-279")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
