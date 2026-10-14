package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorLedgerEngine()
        val item = newDockDoorLedger("code-6", "label-807")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
