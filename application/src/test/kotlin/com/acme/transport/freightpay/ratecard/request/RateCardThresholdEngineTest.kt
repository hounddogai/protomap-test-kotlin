package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardThresholdEngine(newRegistry())
        val item = newRateCardThreshold("code-3", "title-703")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
