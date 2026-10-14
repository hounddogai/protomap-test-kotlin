package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRestBreakOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRestBreakOverrideEngine()
        val item = newWeightedRestBreakOverride("code-129", "label-87")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
