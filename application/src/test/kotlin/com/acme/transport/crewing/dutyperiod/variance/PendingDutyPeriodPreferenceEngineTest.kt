package com.acme.transport.crewing.dutyperiod.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDutyPeriodPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDutyPeriodPreferenceEngine()
        val item = newPendingDutyPeriodPreference("code-20", "label-162")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
