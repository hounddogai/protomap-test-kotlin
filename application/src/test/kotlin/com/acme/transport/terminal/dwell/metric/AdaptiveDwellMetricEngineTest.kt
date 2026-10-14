package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDwellMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDwellMetricEngine(newRegistry())
        val item = newAdaptiveDwellMetric("code-503", "name-336")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
