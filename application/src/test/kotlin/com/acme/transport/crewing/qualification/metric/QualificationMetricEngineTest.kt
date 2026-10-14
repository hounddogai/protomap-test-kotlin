package com.acme.transport.crewing.qualification.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationMetricEngine(newRegistry())
        val item = newQualificationMetric("code-656", "name-918")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
