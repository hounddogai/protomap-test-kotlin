package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDisputeBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDisputeBatchEngine(newRegistry())
        val item = newBatchedDisputeBatch("code-409", "label-437")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
