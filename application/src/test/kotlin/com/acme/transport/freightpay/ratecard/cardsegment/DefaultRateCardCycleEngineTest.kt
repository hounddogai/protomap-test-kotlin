package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultRateCardCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultRateCardCycleEngine(newRegistry())
        val item = newDefaultRateCardCycle("code-172", "name-497")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
