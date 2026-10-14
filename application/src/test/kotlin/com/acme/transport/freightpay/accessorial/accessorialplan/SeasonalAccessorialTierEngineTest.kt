package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalAccessorialTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalAccessorialTierEngine()
        val item = newSeasonalAccessorialTier("code-690", "label-136")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
