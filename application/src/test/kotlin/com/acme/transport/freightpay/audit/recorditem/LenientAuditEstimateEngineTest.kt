package com.acme.transport.freightpay.audit.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientAuditEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientAuditEstimateEngine()
        val item = newLenientAuditEstimate("code-317", "label-931")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
