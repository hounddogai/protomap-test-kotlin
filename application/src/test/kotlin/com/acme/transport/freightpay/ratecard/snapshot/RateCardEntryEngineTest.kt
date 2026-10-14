package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardEntryEngine(newRegistry())
        val item = newRateCardEntry("code-765", "name-179")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
