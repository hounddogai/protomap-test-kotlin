package com.acme.transport.linehaul.backhaul.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalBackhaulMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalBackhaulMetricEngine()
        val item = newGlobalBackhaulMetric("code-441", "label-589")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
