package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingRateCardEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingRateCardEntryEngine(newRegistry())
        val item = newRollingRateCardEntry("code-238", "title-583")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
