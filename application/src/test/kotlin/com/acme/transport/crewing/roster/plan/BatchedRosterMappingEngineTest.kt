package com.acme.transport.crewing.roster.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRosterMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRosterMappingEngine(newRegistry())
        val item = newBatchedRosterMapping("code-584", "name-735")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
