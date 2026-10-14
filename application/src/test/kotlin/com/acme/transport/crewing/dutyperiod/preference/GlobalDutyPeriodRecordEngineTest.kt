package com.acme.transport.crewing.dutyperiod.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDutyPeriodRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDutyPeriodRecordEngine()
        val item = newGlobalDutyPeriodRecord("code-712", "title-934")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
