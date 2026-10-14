package com.acme.transport.linehaul.relay.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayAuditEngine(newRegistry())
        val item = newRelayAudit("code-855", "name-553")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
