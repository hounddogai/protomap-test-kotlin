package com.acme.transport.freightpay.audit.auditbudget

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAuditBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredAuditBudgetEngine()
        val item = newTieredAuditBudget("code-953", "title-264")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
