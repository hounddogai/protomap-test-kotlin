package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditTargetEngine(newRegistry())
        val item = newAuditTarget("code-544", "name-475")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
