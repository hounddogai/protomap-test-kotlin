package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDefectScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDefectScheduleEngine(newRegistry())
        val item = newRegionalDefectSchedule("code-746", "label-299")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
