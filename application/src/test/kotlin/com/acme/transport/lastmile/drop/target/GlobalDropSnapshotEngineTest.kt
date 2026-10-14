package com.acme.transport.lastmile.drop.target

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDropSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDropSnapshotEngine()
        val item = newGlobalDropSnapshot("code-225", "title-901")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
