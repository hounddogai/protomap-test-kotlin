package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingSortationSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingSortationSegmentEngine(newRegistry())
        val item = newPendingSortationSegment("code-402", "name-193")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
