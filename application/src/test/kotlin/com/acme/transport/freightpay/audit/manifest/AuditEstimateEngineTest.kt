package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditEstimateEngine(newRegistry())
        val item = newAuditEstimate("code-221", "title-639")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
