package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyInspectionScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyInspectionScheduleEngine(newRegistry())
        val item = newLegacyInspectionSchedule("code-587", "label-291")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
