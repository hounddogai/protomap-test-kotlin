package com.acme.transport.safety.violation.violationslot

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledViolationTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledViolationTemplateEngine()
        val item = newScheduledViolationTemplate("code-469", "name-472")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
