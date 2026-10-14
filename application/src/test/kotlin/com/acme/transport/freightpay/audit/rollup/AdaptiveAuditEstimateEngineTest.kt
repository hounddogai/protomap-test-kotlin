package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAuditEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAuditEstimateEngine()
        val item = newAdaptiveAuditEstimate("code-558", "title-723")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
