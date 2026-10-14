package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultLockerAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultLockerAuditEngine()
        val item = newDefaultLockerAudit("code-368", "name-926")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
