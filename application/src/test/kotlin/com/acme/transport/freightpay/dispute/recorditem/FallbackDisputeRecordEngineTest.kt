package com.acme.transport.freightpay.dispute.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDisputeRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDisputeRecordEngine(newRegistry())
        val item = newFallbackDisputeRecord("code-221", "name-19")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
