package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRestBreakRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRestBreakRecordEngine(newRegistry())
        val item = newBatchedRestBreakRecord("code-757", "title-371")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
