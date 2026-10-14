package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayAssignmentEngine(newRegistry())
        val item = newRelayAssignment("code-8", "title-657")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
