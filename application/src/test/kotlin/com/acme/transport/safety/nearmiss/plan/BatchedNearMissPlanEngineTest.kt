package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedNearMissPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedNearMissPlanEngine(newRegistry())
        val item = newBatchedNearMissPlan("code-885", "name-179")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
