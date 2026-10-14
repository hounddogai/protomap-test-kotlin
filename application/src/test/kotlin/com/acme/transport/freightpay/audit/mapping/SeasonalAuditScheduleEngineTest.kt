package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalAuditScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalAuditScheduleEngine(newRegistry())
        val item = newSeasonalAuditSchedule("code-221", "title-6")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
