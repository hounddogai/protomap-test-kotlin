package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffAuditEngine(newRegistry())
        val item = newCutoffAudit("code-725", "title-211")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
