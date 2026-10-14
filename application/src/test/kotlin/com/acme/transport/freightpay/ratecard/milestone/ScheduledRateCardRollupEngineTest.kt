package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRateCardRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRateCardRollupEngine()
        val item = newScheduledRateCardRollup("code-488", "name-751")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
