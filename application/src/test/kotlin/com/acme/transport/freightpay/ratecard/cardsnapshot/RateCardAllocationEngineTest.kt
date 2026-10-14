package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardAllocationEngine(newRegistry())
        val item = newRateCardAllocation("code-602", "title-422")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
