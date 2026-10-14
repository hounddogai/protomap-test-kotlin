package com.acme.transport.freightpay.accessorial.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedAccessorialAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedAccessorialAuditEngine()
        val item = newCappedAccessorialAudit("code-652", "label-501")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
