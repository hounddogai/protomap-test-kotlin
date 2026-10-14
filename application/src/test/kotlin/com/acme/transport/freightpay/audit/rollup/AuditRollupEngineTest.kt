package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class AuditRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditRollupEngine()
        val item = newAuditRollup("code-618", "title-505")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
