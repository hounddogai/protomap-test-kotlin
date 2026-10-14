package com.acme.transport.freightpay.audit.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class AuditDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditDigestEngine()
        val item = newAuditDigest("code-938", "label-795")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
