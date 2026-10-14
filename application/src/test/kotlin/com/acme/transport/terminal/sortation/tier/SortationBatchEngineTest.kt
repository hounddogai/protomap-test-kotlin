package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationBatchEngine(newRegistry())
        val item = newSortationBatch("code-799", "name-564")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
