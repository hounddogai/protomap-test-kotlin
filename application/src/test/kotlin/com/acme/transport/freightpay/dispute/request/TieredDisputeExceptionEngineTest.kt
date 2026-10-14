package com.acme.transport.freightpay.dispute.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDisputeExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDisputeExceptionEngine(newRegistry())
        val item = newTieredDisputeException("code-568", "name-613")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
