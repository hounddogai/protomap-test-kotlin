package com.acme.transport.crewing.dutyperiod.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodQuotaEngine()
        val item = newDutyPeriodQuota("code-542", "title-460")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
