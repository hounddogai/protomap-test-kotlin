package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDwellSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDwellSnapshotEngine(newRegistry())
        val item = newScheduledDwellSnapshot("code-292", "label-531")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
