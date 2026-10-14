package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDutyPeriodAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDutyPeriodAgreementEngine()
        val item = newTieredDutyPeriodAgreement("code-61", "label-349")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
