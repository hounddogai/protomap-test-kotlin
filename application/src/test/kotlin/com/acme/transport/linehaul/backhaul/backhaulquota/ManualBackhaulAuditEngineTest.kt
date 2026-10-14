package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualBackhaulAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualBackhaulAuditEngine(newRegistry())
        val item = newManualBackhaulAudit("code-638", "label-451")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
