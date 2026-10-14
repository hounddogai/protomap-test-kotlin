package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalZoneLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalZoneLedgerEngine()
        val item = newRegionalZoneLedger("code-717", "title-431")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
