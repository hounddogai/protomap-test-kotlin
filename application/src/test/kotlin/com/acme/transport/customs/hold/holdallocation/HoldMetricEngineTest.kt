package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldMetricEngine(newRegistry())
        val item = newHoldMetric("code-692", "name-273")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
