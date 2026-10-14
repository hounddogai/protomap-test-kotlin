package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnBaselineEngine(newRegistry())
        val item = newLeaseReturnBaseline("code-631", "label-148")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
