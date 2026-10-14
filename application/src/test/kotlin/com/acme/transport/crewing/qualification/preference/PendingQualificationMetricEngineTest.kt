package com.acme.transport.crewing.qualification.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingQualificationMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingQualificationMetricEngine(newRegistry())
        val item = newPendingQualificationMetric("code-268", "label-760")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
