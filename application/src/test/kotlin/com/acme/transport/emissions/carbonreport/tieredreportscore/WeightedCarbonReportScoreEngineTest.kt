package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCarbonReportScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCarbonReportScoreEngine(newRegistry())
        val item = newWeightedCarbonReportScore("code-988", "label-130")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
