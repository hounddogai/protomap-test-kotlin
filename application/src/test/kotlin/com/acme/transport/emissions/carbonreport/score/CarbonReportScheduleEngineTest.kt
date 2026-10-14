package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportScheduleEngine(newRegistry())
        val item = newCarbonReportSchedule("code-438", "name-393")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
