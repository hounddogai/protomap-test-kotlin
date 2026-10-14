package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictSortationBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictSortationBatchEngine(newRegistry())
        val item = newStrictSortationBatch("code-811", "title-985")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
