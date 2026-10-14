package com.acme.transport.crewing.dutyperiod.periodthreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDutyPeriodThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDutyPeriodThresholdEngine()
        val item = newLegacyDutyPeriodThreshold("code-516", "label-815")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
