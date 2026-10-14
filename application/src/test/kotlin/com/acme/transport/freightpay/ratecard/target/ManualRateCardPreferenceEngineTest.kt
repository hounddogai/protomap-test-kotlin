package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualRateCardPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualRateCardPreferenceEngine()
        val item = newManualRateCardPreference("code-695", "label-670")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
