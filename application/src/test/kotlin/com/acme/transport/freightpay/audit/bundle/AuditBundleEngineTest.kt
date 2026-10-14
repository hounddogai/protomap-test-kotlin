package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditBundleEngine(newRegistry())
        val item = newAuditBundle("code-845", "label-270")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
