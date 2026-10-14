package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledQualificationThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledQualificationThresholdEngine()
        val item = newScheduledQualificationThreshold("code-426", "name-747")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
