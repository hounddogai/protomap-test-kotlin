package com.acme.transport.freightpay.accessorial.request

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialAuditEngine()
        val item = newAccessorialAudit("code-864", "label-944")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
