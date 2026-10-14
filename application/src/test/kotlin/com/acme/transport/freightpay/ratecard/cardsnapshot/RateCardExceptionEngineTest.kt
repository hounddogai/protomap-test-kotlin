package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardExceptionEngine(newRegistry())
        val item = newRateCardException("code-210", "title-415")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
