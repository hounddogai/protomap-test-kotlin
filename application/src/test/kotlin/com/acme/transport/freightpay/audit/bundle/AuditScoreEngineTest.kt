package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditScoreEngine(newRegistry())
        val item = newAuditScore("code-228", "label-694")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
