package com.acme.transport.fleetops.inspection.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionForecastEngine(newRegistry())
        val item = newInspectionForecast("code-484", "name-629")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
