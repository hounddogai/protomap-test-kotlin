package com.acme.transport.crewing.qualification.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationPlanEngine()
        val item = newQualificationPlan("code-539", "label-947")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
