package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyInspectionLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyInspectionLedgerEngine()
        val item = newLegacyInspectionLedger("code-771", "title-549")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
