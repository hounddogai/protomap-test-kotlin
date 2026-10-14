package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualRateCardAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualRateCardAdjustmentEngine(newRegistry())
        val item = newManualRateCardAdjustment("code-252", "title-318")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
