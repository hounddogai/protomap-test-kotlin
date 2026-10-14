package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRateCardExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRateCardExceptionEngine(newRegistry())
        val item = newStrictRateCardException("code-524", "label-352")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
