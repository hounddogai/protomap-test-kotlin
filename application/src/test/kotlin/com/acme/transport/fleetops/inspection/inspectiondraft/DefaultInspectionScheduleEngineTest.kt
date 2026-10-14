package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultInspectionScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultInspectionScheduleEngine(newRegistry())
        val item = newDefaultInspectionSchedule("code-259", "name-34")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
