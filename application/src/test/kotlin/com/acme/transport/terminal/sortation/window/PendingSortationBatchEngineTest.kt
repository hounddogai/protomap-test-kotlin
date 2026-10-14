package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingSortationBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingSortationBatchEngine(newRegistry())
        val item = newPendingSortationBatch("code-337", "name-826")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
