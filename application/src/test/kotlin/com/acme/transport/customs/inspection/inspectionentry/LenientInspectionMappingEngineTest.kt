package com.acme.transport.customs.inspection.inspectionentry

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientInspectionMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientInspectionMappingEngine()
        val item = newLenientInspectionMapping("code-174", "title-709")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
