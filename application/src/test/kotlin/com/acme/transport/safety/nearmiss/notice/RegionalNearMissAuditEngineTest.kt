package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalNearMissAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalNearMissAuditEngine(newRegistry())
        val item = newRegionalNearMissAudit("code-889", "name-549")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
