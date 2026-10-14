package com.acme.transport.fleetops.leasereturn.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnSegmentEngine()
        val item = newLeaseReturnSegment("code-757", "label-959")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
