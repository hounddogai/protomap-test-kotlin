package com.acme.transport.terminal.sortation.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationCheckpointEngine(newRegistry())
        val item = newSortationCheckpoint("code-229", "name-709")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
