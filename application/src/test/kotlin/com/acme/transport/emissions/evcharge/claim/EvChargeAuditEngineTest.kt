package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeAuditEngine()
        val item = newEvChargeAudit("code-68", "label-46")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
