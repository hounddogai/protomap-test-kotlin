package com.acme.transport.network.cutoff.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCutoffLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCutoffLedgerEngine()
        val item = newManualCutoffLedger("code-745", "label-749")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
