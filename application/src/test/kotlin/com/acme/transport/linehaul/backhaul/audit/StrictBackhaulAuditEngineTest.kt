package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictBackhaulAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictBackhaulAuditEngine(newRegistry())
        val item = newStrictBackhaulAudit("code-39", "title-191")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
