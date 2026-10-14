package com.acme.transport.crewing.qualification.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationEstimateEngine()
        val item = newQualificationEstimate("code-185", "name-369")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
