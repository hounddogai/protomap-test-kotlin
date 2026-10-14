package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRateCardBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRateCardBundleEngine(newRegistry())
        val item = newGlobalRateCardBundle("code-186", "title-322")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
