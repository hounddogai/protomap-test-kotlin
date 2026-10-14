package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyNodeMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyNodeMetricEngine(newRegistry())
        val item = newLegacyNodeMetric("code-657", "label-311")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
