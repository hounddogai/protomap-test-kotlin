package com.acme.transport.fleetops.leasereturn.returnaudit

import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnRequestEngine()
        val item = newLeaseReturnRequest("code-636", "title-902")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
