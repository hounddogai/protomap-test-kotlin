package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodRequestEngine(newRegistry())
        val item = newDutyPeriodRequest("code-196", "name-158")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
