package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationMilestoneEngine()
        val item = newQualificationMilestone("code-564", "name-235")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
