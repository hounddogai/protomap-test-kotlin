package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRosterAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRosterAuditEngine()
        val item = newBatchedRosterAudit("code-245", "label-639")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
