package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertTrue

class AuditForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditForecastEngine()
        val item = newAuditForecast("code-307", "label-238")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
