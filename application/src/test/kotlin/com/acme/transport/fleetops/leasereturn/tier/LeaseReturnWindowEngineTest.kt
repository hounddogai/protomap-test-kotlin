package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnWindowEngine(newRegistry())
        val item = newLeaseReturnWindow("code-980", "label-103")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
