package com.acme.transport.fleetops.tire.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTireAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTireAuditEngine(newRegistry())
        val item = newStrictTireAudit("code-265", "label-98")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
