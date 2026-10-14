package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedAuditPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedAuditPolicyEngine()
        val item = newWeightedAuditPolicy("code-106", "name-642")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
