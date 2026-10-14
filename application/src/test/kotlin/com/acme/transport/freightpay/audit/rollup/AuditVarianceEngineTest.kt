package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditVarianceEngine(newRegistry())
        val item = newAuditVariance("code-116", "title-518")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
