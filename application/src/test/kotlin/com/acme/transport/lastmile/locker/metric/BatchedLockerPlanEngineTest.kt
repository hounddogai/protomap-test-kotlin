package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedLockerPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedLockerPlanEngine(newRegistry())
        val item = newBatchedLockerPlan("code-954", "label-870")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
