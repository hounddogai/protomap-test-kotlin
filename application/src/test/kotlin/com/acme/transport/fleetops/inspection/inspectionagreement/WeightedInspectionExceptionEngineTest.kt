package com.acme.transport.fleetops.inspection.inspectionagreement

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedInspectionExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedInspectionExceptionEngine()
        val item = newWeightedInspectionException("code-675", "label-194")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
