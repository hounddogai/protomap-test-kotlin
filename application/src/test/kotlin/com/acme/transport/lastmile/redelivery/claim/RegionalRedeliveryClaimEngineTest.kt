package com.acme.transport.lastmile.redelivery.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRedeliveryClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRedeliveryClaimEngine()
        val item = newRegionalRedeliveryClaim("code-235", "label-387")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
