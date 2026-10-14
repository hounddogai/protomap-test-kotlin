package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnMappingEngine(newRegistry())
        val item = newLeaseReturnMapping("code-705", "label-137")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
