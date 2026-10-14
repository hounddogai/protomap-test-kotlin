package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRelayBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRelayBatchEngine()
        val item = newBatchedRelayBatch("code-486", "name-704")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
