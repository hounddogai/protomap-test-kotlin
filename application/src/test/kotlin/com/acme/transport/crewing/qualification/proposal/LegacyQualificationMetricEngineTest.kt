package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyQualificationMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyQualificationMetricEngine()
        val item = newLegacyQualificationMetric("code-817", "label-793")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
