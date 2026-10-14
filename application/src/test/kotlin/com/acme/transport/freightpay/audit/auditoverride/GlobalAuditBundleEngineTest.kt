package com.acme.transport.freightpay.audit.auditoverride

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalAuditBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalAuditBundleEngine(newRegistry())
        val item = newGlobalAuditBundle("code-522", "title-969")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
