package com.acme.transport.crewing.dutyperiod.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodSlotEngine()
        val item = newDutyPeriodSlot("code-352", "name-923")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
