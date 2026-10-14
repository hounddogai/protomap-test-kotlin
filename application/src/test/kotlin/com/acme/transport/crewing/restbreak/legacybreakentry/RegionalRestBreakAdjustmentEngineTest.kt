package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRestBreakAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRestBreakAdjustmentEngine()
        val item = newRegionalRestBreakAdjustment("code-281", "name-156")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
