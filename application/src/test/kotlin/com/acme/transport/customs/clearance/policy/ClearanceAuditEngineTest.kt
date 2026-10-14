package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceAuditEngine()
        val item = newClearanceAudit("code-370", "title-252")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
