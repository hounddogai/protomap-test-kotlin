package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDwellSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDwellSnapshotEngine(newRegistry())
        val item = newPrimaryDwellSnapshot("code-518", "label-79")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
