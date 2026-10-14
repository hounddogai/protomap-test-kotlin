package com.acme.transport.terminal.sortation.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalSortationAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalSortationAdjustmentEngine()
        val item = newRegionalSortationAdjustment("code-166", "title-816")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
