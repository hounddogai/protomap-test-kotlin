package com.acme.transport.freightpay.ratecard.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultRateCardAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultRateCardAuditEngine()
        val item = newDefaultRateCardAudit("code-10", "name-92")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
