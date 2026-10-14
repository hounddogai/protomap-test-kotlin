package com.acme.transport.network.hub.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HubMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubMetricEngine(newRegistry())
        val item = newHubMetric("code-228", "name-769")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
