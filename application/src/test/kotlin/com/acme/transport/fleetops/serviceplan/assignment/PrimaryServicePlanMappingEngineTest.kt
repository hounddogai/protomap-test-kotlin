package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryServicePlanMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryServicePlanMappingEngine(newRegistry())
        val item = newPrimaryServicePlanMapping("code-174", "title-422")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
