package com.acme.transport.freightpay.ratecard.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultRateCardEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultRateCardEntryEngine()
        val item = newDefaultRateCardEntry("code-546", "title-386")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
