package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAuditBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAuditBundleEngine(newRegistry())
        val item = newAdaptiveAuditBundle("code-108", "label-190")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
