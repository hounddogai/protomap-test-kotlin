package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDutyPeriodEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultDutyPeriodEntryEngine()
        val item = newDefaultDutyPeriodEntry("code-726", "label-966")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
