package com.acme.transport.fleetops.defect.template

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDefectTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDefectTemplateEngine()
        val item = newAdaptiveDefectTemplate("code-307", "title-360")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
