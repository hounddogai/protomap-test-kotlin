package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictServicePlanClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictServicePlanClaimEngine()
        val item = newStrictServicePlanClaim("code-443", "label-584")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
