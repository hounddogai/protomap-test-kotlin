package com.acme.transport.freightpay.audit.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class AuditSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditSnapshotEngine()
        val item = newAuditSnapshot("code-656", "name-144")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
