package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertTrue

class DefectTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectTemplateEngine()
        val item = newDefectTemplate("code-236", "name-707")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
