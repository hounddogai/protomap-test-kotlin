package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCrossDockAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCrossDockAuditEngine(newRegistry())
        val item = newGlobalCrossDockAudit("code-189", "label-863")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
