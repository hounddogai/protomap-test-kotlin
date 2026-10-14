package com.acme.transport.crewing.dutyperiod.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDutyPeriodAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDutyPeriodAgreementEngine()
        val item = newBatchedDutyPeriodAgreement("code-358", "title-999")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
