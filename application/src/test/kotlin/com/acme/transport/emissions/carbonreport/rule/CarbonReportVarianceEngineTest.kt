package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportVarianceEngine(newRegistry())
        val item = newCarbonReportVariance("code-941", "label-352")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
