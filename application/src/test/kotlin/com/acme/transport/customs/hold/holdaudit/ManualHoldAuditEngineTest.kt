package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualHoldAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualHoldAuditEngine(newRegistry())
        val item = newManualHoldAudit("code-955", "name-884")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
