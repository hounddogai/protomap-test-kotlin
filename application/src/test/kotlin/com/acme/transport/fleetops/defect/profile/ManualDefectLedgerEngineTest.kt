package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDefectLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDefectLedgerEngine(newRegistry())
        val item = newManualDefectLedger("code-658", "label-742")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
