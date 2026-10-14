package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyAuditRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyAuditRuleEngine(newRegistry())
        val item = newLegacyAuditRule("code-864", "name-672")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
