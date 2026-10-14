package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionBaselineEngine(newRegistry())
        val item = newInspectionBaseline("code-720", "label-477")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
