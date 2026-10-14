package com.acme.transport.crewing.restbreak.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRestBreakLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRestBreakLedgerEngine()
        val item = newSeasonalRestBreakLedger("code-283", "title-227")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
