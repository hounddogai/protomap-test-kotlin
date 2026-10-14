package com.acme.transport.freightpay.ratecard.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRateCardRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRateCardRollupEngine()
        val item = newWeightedRateCardRollup("code-687", "name-240")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
