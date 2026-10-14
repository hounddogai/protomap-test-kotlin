package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityLedgerEngine(newRegistry())
        val item = newCapacityLedger("code-118", "label-435")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
