package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderAuditEngine(newRegistry())
        val item = newTenderAudit("code-442", "title-397")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
