package com.acme.transport.freightpay.audit.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class AuditOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditOverrideEngine()
        val item = newAuditOverride("code-301", "label-792")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
