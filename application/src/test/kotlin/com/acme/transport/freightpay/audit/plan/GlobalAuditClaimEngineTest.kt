package com.acme.transport.freightpay.audit.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalAuditClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalAuditClaimEngine(newRegistry())
        val item = newGlobalAuditClaim("code-416", "label-574")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
