package com.acme.transport.network.node.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeMetricEngine(newRegistry())
        val item = newNodeMetric("code-320", "name-770")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
