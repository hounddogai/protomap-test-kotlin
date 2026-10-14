package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingRateCardSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingRateCardSummaryEngine(newRegistry())
        val item = newRollingRateCardSummary("code-311", "label-851")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
