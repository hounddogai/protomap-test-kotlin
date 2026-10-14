package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledQualificationTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledQualificationTemplateEngine()
        val item = newScheduledQualificationTemplate("code-289", "label-947")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
