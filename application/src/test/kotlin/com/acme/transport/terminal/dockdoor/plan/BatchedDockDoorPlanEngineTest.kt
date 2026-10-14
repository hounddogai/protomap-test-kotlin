package com.acme.transport.terminal.dockdoor.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDockDoorPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDockDoorPlanEngine()
        val item = newBatchedDockDoorPlan("code-855", "title-445")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
