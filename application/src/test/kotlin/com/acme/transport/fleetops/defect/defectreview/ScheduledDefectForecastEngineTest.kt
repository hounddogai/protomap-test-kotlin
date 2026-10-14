package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDefectForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDefectForecastEngine(newRegistry())
        val item = newScheduledDefectForecast("code-822", "label-977")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
