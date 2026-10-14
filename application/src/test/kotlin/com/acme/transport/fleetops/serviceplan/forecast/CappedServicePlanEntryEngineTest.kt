package com.acme.transport.fleetops.serviceplan.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedServicePlanEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedServicePlanEntryEngine()
        val item = newCappedServicePlanEntry("code-465", "title-853")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
