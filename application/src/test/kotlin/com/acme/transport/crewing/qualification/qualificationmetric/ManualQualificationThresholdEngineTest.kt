package com.acme.transport.crewing.qualification.qualificationmetric

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualQualificationThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualQualificationThresholdEngine()
        val item = newManualQualificationThreshold("code-384", "name-90")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
