package com.acme.transport.lastmile.parcel.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelPlanEngine()
        val item = newParcelPlan("code-58", "name-108")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
