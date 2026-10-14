package com.acme.transport.crewing.dutyperiod.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodBundleEngine()
        val item = newDutyPeriodBundle("code-777", "name-634")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
