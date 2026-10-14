package com.acme.transport.fleetops.utilization.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualUtilizationVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualUtilizationVarianceEngine()
        val item = newManualUtilizationVariance("code-554", "label-245")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
