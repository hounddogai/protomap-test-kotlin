package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityAuditEngine(newRegistry())
        val item = newCapacityAudit("code-894", "title-903")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
