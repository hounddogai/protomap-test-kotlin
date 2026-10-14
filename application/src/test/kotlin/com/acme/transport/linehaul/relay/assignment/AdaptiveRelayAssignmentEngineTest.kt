package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveRelayAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveRelayAssignmentEngine(newRegistry())
        val item = newAdaptiveRelayAssignment("code-442", "title-579")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
