package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionAdjustmentEngine(newRegistry())
        val item = newInspectionAdjustment("code-836", "label-333")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
