package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledEmissionFactorMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledEmissionFactorMetricEngine(newRegistry())
        val item = newScheduledEmissionFactorMetric("code-531", "label-211")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
