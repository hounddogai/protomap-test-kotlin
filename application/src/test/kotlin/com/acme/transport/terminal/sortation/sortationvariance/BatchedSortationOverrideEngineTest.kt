package com.acme.transport.terminal.sortation.sortationvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedSortationOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedSortationOverrideEngine()
        val item = newBatchedSortationOverride("code-901", "label-12")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
