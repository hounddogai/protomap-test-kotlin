package com.acme.transport.freightpay.audit.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalAuditDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalAuditDraftEngine()
        val item = newSeasonalAuditDraft("code-973", "label-855")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
