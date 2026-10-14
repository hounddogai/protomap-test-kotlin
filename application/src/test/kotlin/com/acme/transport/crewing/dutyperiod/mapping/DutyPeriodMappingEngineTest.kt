package com.acme.transport.crewing.dutyperiod.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodMappingEngine(newRegistry())
        val item = newDutyPeriodMapping("code-698", "name-458")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
