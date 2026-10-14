package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDutyPeriodDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDutyPeriodDraftEngine()
        val item = newPrimaryDutyPeriodDraft("code-684", "label-379")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
