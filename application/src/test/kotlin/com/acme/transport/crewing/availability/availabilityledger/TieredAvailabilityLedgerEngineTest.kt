package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAvailabilityLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredAvailabilityLedgerEngine(newRegistry())
        val item = newTieredAvailabilityLedger("code-539", "label-628")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
