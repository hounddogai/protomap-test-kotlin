package com.acme.transport.freightpay.audit.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyAuditManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyAuditManifestEngine()
        val item = newLegacyAuditManifest("code-610", "label-649")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
