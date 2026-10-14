package com.acme.transport.fleetops.inspection.inspectionbatch

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingInspectionDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingInspectionDraftEngine()
        val item = newRollingInspectionDraft("code-912", "title-550")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
