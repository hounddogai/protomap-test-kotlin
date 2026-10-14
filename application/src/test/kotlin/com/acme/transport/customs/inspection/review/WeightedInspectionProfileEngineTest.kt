package com.acme.transport.customs.inspection.review

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedInspectionProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedInspectionProfileEngine()
        val item = newWeightedInspectionProfile("code-58", "name-558")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
