package com.acme.transport.emissions.emissionfactor.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorMetricEngine()
        val item = newEmissionFactorMetric("code-845", "label-41")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
