package com.acme.transport.freightpay.ratecard.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardTemplateEngine(newRegistry())
        val item = newRateCardTemplate("code-536", "label-30")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
