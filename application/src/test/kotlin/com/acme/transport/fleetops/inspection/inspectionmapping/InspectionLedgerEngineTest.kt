package com.acme.transport.fleetops.inspection.inspectionmapping

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionLedgerEngine()
        val item = newInspectionLedger("code-478", "title-185")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
