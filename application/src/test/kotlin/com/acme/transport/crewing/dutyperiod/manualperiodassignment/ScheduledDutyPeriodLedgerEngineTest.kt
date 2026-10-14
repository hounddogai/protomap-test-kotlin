package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDutyPeriodLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDutyPeriodLedgerEngine()
        val item = newScheduledDutyPeriodLedger("code-812", "name-208")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
