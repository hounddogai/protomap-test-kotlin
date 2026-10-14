package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationAssignmentEngine()
        val item = newUtilizationAssignment("code-97", "title-135")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
