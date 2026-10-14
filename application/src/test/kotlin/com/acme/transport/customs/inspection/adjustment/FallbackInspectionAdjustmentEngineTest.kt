package com.acme.transport.customs.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackInspectionAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackInspectionAdjustmentEngine()
        val item = newFallbackInspectionAdjustment("code-293", "name-907")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
