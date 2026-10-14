package com.acme.transport.crewing.dutyperiod.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDutyPeriodThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDutyPeriodThresholdEngine()
        val item = newManualDutyPeriodThreshold("code-725", "label-246")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
