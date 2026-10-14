package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRateCardEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRateCardEntryEngine(newRegistry())
        val item = newGlobalRateCardEntry("code-787", "name-33")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
