package com.acme.transport.customs.inspection.inspectionentry

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionQuotaEngine()
        val item = newInspectionQuota("code-51", "label-627")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
