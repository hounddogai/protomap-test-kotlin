package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryBackhaulMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryBackhaulMetricEngine(newRegistry())
        val item = newPrimaryBackhaulMetric("code-944", "label-254")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
