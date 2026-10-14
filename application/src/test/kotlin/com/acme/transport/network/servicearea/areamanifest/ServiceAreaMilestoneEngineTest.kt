package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaMilestoneEngine()
        val item = newServiceAreaMilestone("code-603", "title-535")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
