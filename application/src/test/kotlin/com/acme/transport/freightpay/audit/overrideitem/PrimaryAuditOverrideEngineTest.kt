package com.acme.transport.freightpay.audit.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryAuditOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryAuditOverrideEngine()
        val item = newPrimaryAuditOverride("code-209", "label-170")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
