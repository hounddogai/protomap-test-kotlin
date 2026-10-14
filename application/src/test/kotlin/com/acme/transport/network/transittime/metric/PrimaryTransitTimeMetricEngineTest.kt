package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTransitTimeMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTransitTimeMetricEngine(newRegistry())
        val item = newPrimaryTransitTimeMetric("code-421", "name-337")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
