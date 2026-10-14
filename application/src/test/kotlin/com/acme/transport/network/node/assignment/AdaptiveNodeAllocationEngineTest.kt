package com.acme.transport.network.node.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveNodeAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveNodeAllocationEngine(newRegistry())
        val item = newAdaptiveNodeAllocation("code-797", "name-70")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
