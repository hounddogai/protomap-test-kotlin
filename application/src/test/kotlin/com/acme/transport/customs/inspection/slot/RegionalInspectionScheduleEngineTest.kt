package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalInspectionScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalInspectionScheduleEngine(newRegistry())
        val item = newRegionalInspectionSchedule("code-130", "name-81")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
