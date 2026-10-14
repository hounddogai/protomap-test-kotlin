package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardBudgetEngine()
        val item = newRateCardBudget("code-575", "title-49")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
