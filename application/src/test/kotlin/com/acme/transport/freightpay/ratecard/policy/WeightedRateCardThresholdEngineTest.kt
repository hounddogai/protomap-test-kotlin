package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRateCardThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRateCardThresholdEngine(newRegistry())
        val item = newWeightedRateCardThreshold("code-631", "name-918")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
