package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardScoreEngine(newRegistry())
        val item = newRateCardScore("code-497", "label-66")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
