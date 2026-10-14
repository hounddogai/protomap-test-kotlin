package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedLeaseReturnRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedLeaseReturnRuleEngine()
        val item = newBatchedLeaseReturnRule("code-249", "title-169")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
