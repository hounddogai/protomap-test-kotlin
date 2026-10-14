package com.acme.transport.freightpay.ratecard.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardMetricEngine(newRegistry())
        val item = newRateCardMetric("code-864", "title-352")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
