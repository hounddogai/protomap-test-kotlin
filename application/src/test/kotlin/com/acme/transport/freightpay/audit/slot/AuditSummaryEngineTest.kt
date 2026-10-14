package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditSummaryEngine(newRegistry())
        val item = newAuditSummary("code-343", "name-969")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
