package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnRecordEngine()
        val item = newLeaseReturnRecord("code-273", "title-590")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
