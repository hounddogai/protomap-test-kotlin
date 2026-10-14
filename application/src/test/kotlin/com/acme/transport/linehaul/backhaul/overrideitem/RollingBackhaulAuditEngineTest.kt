package com.acme.transport.linehaul.backhaul.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingBackhaulAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingBackhaulAuditEngine()
        val item = newRollingBackhaulAudit("code-60", "label-856")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
