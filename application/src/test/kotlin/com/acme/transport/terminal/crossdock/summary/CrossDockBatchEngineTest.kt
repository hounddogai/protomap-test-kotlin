package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockBatchEngine(newRegistry())
        val item = newCrossDockBatch("code-367", "name-533")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
