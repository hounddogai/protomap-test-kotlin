package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTrailerYardAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTrailerYardAdjustmentEngine()
        val item = newManualTrailerYardAdjustment("code-180", "name-508")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
