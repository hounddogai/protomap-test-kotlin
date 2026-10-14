package com.acme.transport.freightpay.audit.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAuditManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAuditManifestEngine(newRegistry())
        val item = newAdaptiveAuditManifest("code-544", "name-547")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
