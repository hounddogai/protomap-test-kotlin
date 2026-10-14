package com.acme.transport.emissions.emissionfactor.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalEmissionFactorSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalEmissionFactorSummaryEngine()
        val item = newGlobalEmissionFactorSummary("code-493", "label-962")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
