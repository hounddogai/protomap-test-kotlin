package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAuditManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredAuditManifestEngine(newRegistry())
        val item = newTieredAuditManifest("code-247", "title-244")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
