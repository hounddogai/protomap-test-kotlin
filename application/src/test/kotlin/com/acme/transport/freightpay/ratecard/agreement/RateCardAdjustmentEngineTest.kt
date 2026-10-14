package com.acme.transport.freightpay.ratecard.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardAdjustmentEngine(newRegistry())
        val item = newRateCardAdjustment("code-884", "name-422")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
