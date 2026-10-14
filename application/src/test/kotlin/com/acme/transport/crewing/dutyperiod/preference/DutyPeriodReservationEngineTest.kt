package com.acme.transport.crewing.dutyperiod.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodReservationEngine()
        val item = newDutyPeriodReservation("code-681", "title-538")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
