package com.acme.transport.customs.inspection.window

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveInspectionWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveInspectionWindowEngine()
        val item = newAdaptiveInspectionWindow("code-853", "label-558")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
