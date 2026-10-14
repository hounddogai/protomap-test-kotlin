package com.acme.transport.fleetops.utilization.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientUtilizationAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientUtilizationAssignmentEngine(newRegistry())
        val item = newLenientUtilizationAssignment("code-931", "label-187")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
