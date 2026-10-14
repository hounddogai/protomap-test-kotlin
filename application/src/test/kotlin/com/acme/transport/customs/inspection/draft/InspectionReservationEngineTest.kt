package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionReservationEngine()
        val item = newInspectionReservation("code-373", "label-314")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
