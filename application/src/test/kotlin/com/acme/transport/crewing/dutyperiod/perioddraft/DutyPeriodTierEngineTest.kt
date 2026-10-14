package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodTierEngine(newRegistry())
        val item = newDutyPeriodTier("code-170", "label-368")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
