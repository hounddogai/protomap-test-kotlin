package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionLedgerEngine(newRegistry())
        val item = newInspectionLedger("code-163", "name-611")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
