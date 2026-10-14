package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingEvChargeMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingEvChargeMetricEngine(newRegistry())
        val item = newRollingEvChargeMetric("code-57", "label-639")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
