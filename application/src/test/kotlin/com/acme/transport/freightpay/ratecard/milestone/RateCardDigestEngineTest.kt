package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardDigestEngine(newRegistry())
        val item = newRateCardDigest("code-623", "label-557")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
