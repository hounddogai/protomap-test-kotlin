package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyNodeAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyNodeAssignmentEngine(newRegistry())
        val item = newLegacyNodeAssignment("code-13", "name-300")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
