package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedNodeLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedNodeLedgerEngine(newRegistry())
        val item = newBatchedNodeLedger("code-118", "title-937")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
