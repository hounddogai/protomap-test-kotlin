package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledCrossDockAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledCrossDockAssignmentEngine(newRegistry())
        val item = newScheduledCrossDockAssignment("code-525", "title-603")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
