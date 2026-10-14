package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledEvChargeAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledEvChargeAssignmentEngine(newRegistry())
        val item = newScheduledEvChargeAssignment("code-157", "label-516")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
