package com.acme.transport.freightpay.audit.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditExceptionEngine(newRegistry())
        val item = newAuditException("code-720", "name-896")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
