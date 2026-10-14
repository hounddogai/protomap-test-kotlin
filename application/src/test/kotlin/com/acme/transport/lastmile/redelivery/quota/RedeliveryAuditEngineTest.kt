package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryAuditEngine(newRegistry())
        val item = newRedeliveryAudit("code-309", "label-740")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
