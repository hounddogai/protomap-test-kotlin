package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardCycleEngine(newRegistry())
        val item = newRateCardCycle("code-791", "label-684")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
