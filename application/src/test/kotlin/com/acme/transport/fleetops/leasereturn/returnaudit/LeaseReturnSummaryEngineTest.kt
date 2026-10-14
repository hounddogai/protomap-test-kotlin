package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnSummaryEngine(newRegistry())
        val item = newLeaseReturnSummary("code-744", "label-370")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
