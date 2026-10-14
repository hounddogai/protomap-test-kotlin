package com.acme.transport.crewing.restbreak.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRestBreakSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRestBreakSnapshotEngine()
        val item = newGlobalRestBreakSnapshot("code-917", "title-290")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
