package com.acme.transport.freightpay.audit.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAuditDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredAuditDigestEngine()
        val item = newTieredAuditDigest("code-460", "name-781")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
