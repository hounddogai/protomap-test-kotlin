package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneAuditEngine(newRegistry())
        val item = newZoneAudit("code-611", "name-99")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
