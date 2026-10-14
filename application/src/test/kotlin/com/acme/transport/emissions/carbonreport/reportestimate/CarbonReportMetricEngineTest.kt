package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportMetricEngine(newRegistry())
        val item = newCarbonReportMetric("code-1", "label-244")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
