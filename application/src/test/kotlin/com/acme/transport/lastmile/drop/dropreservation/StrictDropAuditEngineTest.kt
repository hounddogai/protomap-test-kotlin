package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDropAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDropAuditEngine(newRegistry())
        val item = newStrictDropAudit("code-712", "title-517")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
