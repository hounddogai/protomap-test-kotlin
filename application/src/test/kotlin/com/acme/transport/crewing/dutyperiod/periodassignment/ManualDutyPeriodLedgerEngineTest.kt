package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDutyPeriodLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDutyPeriodLedgerEngine(newRegistry())
        val item = newManualDutyPeriodLedger("code-696", "label-975")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
