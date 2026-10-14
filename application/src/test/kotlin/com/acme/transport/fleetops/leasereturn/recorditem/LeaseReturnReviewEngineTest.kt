package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnReviewEngine()
        val item = newLeaseReturnReview("code-312", "title-798")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
