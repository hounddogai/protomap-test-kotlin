package com.acme.transport.freightpay.ratecard.cardsnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRateCardSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRateCardSnapshotEngine()
        val item = newSeasonalRateCardSnapshot("code-73", "name-206")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
