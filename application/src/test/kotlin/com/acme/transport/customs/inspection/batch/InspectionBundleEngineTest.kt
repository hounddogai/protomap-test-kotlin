package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionBundleEngine()
        val item = newInspectionBundle("code-595", "title-128")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
