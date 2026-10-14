package com.acme.transport.emissions.carbonreport.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCarbonReportTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCarbonReportTierEngine(newRegistry())
        val item = newWeightedCarbonReportTier("code-357", "title-186")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
