package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditProfileEngine(newRegistry())
        val item = newAuditProfile("code-539", "name-962")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
