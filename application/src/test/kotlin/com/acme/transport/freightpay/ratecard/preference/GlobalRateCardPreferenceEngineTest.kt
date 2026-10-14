package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRateCardPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRateCardPreferenceEngine(newRegistry())
        val item = newGlobalRateCardPreference("code-910", "name-591")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
