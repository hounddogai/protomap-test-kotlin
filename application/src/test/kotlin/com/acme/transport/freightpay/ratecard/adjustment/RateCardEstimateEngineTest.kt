package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardEstimateEngine(newRegistry())
        val item = newRateCardEstimate("code-584", "name-532")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
