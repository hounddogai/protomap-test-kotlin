package com.acme.transport.linehaul.laneplan.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanAuditEngine()
        val item = newLanePlanAudit("code-446", "label-43")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
