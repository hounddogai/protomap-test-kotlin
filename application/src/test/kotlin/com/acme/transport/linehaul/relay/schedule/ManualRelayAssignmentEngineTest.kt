package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualRelayAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualRelayAssignmentEngine(newRegistry())
        val item = newManualRelayAssignment("code-9", "title-658")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
