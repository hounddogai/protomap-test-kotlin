package com.acme.transport.emissions.carbonreport.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportReservationEngine(newRegistry())
        val item = newCarbonReportReservation("code-564", "name-159")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
