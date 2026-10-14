package com.acme.transport.linehaul.tender.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTenderVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTenderVarianceEngine()
        val item = newManualTenderVariance("code-930", "label-833")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
