package com.acme.transport.crewing.availability.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityLedgerEngine(newRegistry())
        val item = newAvailabilityLedger("code-196", "label-322")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
