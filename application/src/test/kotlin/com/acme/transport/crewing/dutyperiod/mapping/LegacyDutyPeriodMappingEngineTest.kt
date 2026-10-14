package com.acme.transport.crewing.dutyperiod.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDutyPeriodMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDutyPeriodMappingEngine(newRegistry())
        val item = newLegacyDutyPeriodMapping("code-122", "title-10")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
