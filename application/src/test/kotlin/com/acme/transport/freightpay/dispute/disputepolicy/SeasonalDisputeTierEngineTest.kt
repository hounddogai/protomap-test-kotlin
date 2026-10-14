package com.acme.transport.freightpay.dispute.disputepolicy

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDisputeTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDisputeTierEngine()
        val item = newSeasonalDisputeTier("code-422", "title-938")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
