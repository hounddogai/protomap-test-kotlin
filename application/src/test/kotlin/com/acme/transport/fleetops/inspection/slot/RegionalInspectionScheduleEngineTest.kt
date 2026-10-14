package com.acme.transport.fleetops.inspection.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalInspectionScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalInspectionScheduleEngine(newRegistry())
        val item = newRegionalInspectionSchedule("code-342", "label-99")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
