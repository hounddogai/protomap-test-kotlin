package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaPlanEngine(newRegistry())
        val item = newServiceAreaPlan("code-52", "title-246")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
