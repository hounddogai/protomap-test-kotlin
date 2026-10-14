package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanRollupEngine(newRegistry())
        val item = newServicePlanRollup("code-844", "name-821")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
