package com.acme.transport.customs.hold.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalHoldAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalHoldAuditEngine(newRegistry())
        val item = newSeasonalHoldAudit("code-39", "title-777")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
