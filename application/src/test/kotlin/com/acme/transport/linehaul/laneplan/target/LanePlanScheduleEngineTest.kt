package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanScheduleEngine(newRegistry())
        val item = newLanePlanSchedule("code-567", "label-457")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
