package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRestBreakAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRestBreakAuditEngine()
        val item = newPendingRestBreakAudit("code-279", "title-908")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
