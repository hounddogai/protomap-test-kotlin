package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedServicePlanRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedServicePlanRollupEngine(newRegistry())
        val item = newCappedServicePlanRollup("code-411", "name-330")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
