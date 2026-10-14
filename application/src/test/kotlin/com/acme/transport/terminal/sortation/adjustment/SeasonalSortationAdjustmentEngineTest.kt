package com.acme.transport.terminal.sortation.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalSortationAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalSortationAdjustmentEngine()
        val item = newSeasonalSortationAdjustment("code-883", "name-260")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
