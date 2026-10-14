package com.acme.transport.crewing.restbreak.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakAdjustmentEngine()
        val item = newRestBreakAdjustment("code-909", "label-695")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
