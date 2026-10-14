package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCarbonReportVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCarbonReportVarianceEngine(newRegistry())
        val item = newLenientCarbonReportVariance("code-404", "label-101")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
