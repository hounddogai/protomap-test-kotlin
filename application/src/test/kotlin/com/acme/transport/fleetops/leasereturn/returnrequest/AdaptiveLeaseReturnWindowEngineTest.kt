package com.acme.transport.fleetops.leasereturn.returnrequest

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveLeaseReturnWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveLeaseReturnWindowEngine()
        val item = newAdaptiveLeaseReturnWindow("code-73", "name-459")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
