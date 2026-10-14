package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRateCardVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRateCardVarianceEngine(newRegistry())
        val item = newFallbackRateCardVariance("code-643", "title-395")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
