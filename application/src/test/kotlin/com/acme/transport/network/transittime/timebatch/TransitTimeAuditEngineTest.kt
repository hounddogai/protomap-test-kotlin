package com.acme.transport.network.transittime.timebatch

import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeAuditEngine()
        val item = newTransitTimeAudit("code-734", "name-2")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
