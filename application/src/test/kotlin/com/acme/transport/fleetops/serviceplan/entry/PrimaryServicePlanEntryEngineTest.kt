package com.acme.transport.fleetops.serviceplan.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryServicePlanEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryServicePlanEntryEngine()
        val item = newPrimaryServicePlanEntry("code-897", "name-493")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
