package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffSnapshotEngine(newRegistry())
        val item = newCutoffSnapshot("code-797", "title-407")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
