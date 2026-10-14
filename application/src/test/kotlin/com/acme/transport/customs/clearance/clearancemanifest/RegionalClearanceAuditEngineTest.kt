package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalClearanceAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalClearanceAuditEngine(newRegistry())
        val item = newRegionalClearanceAudit("code-625", "name-329")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
