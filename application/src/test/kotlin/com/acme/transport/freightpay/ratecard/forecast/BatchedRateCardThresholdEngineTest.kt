package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRateCardThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRateCardThresholdEngine(newRegistry())
        val item = newBatchedRateCardThreshold("code-704", "name-965")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
