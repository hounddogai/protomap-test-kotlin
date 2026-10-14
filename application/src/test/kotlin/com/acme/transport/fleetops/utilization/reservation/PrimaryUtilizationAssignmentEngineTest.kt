package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryUtilizationAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryUtilizationAssignmentEngine(newRegistry())
        val item = newPrimaryUtilizationAssignment("code-821", "title-968")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
