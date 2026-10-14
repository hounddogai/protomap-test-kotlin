package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnVarianceEngine(newRegistry())
        val item = newLeaseReturnVariance("code-631", "name-674")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
