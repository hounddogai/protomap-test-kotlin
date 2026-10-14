package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualRateCardSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualRateCardSummaryEngine(newRegistry())
        val item = newManualRateCardSummary("code-353", "label-888")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
