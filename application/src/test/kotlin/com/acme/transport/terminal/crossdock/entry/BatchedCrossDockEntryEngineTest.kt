package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCrossDockEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCrossDockEntryEngine(newRegistry())
        val item = newBatchedCrossDockEntry("code-923", "label-465")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
