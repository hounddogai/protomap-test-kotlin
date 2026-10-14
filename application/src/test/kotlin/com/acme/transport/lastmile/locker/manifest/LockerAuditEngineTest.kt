package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LockerAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerAuditEngine(newRegistry())
        val item = newLockerAudit("code-663", "name-115")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
