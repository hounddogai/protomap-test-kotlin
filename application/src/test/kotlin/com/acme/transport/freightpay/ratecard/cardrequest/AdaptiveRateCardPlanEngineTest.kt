package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveRateCardPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveRateCardPlanEngine(newRegistry())
        val item = newAdaptiveRateCardPlan("code-492", "name-475")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
