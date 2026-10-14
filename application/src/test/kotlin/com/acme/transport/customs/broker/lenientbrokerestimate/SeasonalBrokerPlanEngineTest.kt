package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalBrokerPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalBrokerPlanEngine(newRegistry())
        val item = newSeasonalBrokerPlan("code-498", "name-471")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
