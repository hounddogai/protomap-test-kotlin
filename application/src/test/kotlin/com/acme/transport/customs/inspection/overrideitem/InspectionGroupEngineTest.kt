package com.acme.transport.customs.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionGroupEngine()
        val item = newInspectionGroup("code-596", "label-484")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
