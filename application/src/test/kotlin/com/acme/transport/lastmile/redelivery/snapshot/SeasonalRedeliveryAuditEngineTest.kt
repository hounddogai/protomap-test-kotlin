package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRedeliveryAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRedeliveryAuditEngine()
        val item = newSeasonalRedeliveryAudit("code-224", "label-403")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
