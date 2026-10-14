package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardBundleEngine(newRegistry())
        val item = newRateCardBundle("code-278", "name-792")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
