package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTrailerMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTrailerMetricEngine(newRegistry())
        val item = newManualTrailerMetric("code-627", "name-657")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
