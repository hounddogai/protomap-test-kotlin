package com.acme.transport.crewing.swap.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualSwapMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualSwapMetricEngine(newRegistry())
        val item = newManualSwapMetric("code-21", "label-85")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
