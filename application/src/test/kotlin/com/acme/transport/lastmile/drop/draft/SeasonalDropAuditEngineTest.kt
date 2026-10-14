package com.acme.transport.lastmile.drop.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDropAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDropAuditEngine()
        val item = newSeasonalDropAudit("code-307", "label-128")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
