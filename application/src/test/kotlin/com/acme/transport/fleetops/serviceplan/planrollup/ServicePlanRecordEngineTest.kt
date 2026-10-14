package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanRecordEngine(newRegistry())
        val item = newServicePlanRecord("code-84", "name-314")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
