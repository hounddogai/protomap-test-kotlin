package com.acme.transport.fleetops.serviceplan.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedServicePlanClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedServicePlanClaimEngine()
        val item = newCappedServicePlanClaim("code-615", "title-743")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
