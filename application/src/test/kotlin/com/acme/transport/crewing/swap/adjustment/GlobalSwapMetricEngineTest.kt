package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalSwapMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalSwapMetricEngine()
        val item = newGlobalSwapMetric("code-533", "title-933")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
