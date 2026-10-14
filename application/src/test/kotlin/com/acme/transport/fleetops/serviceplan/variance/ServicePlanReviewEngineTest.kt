package com.acme.transport.fleetops.serviceplan.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanReviewEngine()
        val item = newServicePlanReview("code-469", "label-197")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
