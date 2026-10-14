package com.acme.transport.terminal.dwell.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDwellAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDwellAdjustmentEngine()
        val item = newStrictDwellAdjustment("code-817", "label-790")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
