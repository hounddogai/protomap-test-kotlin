package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class SortationAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationAdjustmentEngine()
        val item = newSortationAdjustment("code-808", "label-564")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
