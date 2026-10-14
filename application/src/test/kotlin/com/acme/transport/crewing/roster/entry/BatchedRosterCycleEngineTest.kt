package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRosterCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRosterCycleEngine()
        val item = newBatchedRosterCycle("code-799", "label-572")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
