package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledServicePlanTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledServicePlanTierEngine(newRegistry())
        val item = newScheduledServicePlanTier("code-520", "title-174")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
