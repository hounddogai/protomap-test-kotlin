package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDisputeRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDisputeRollupEngine(newRegistry())
        val item = newFallbackDisputeRollup("code-590", "title-762")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
