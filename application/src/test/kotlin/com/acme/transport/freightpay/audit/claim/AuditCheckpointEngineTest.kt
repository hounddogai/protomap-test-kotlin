package com.acme.transport.freightpay.audit.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class AuditCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditCheckpointEngine()
        val item = newAuditCheckpoint("code-462", "name-444")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
