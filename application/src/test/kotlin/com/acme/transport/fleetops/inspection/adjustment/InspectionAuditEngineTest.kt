package com.acme.transport.fleetops.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionAuditEngine()
        val item = newInspectionAudit("code-209", "title-201")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
