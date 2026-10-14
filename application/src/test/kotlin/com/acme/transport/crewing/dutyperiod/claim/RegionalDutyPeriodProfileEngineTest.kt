package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDutyPeriodProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDutyPeriodProfileEngine(newRegistry())
        val item = newRegionalDutyPeriodProfile("code-164", "name-619")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
