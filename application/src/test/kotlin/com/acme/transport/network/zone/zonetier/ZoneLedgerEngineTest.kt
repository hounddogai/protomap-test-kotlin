package com.acme.transport.network.zone.zonetier

import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneLedgerEngine()
        val item = newZoneLedger("code-221", "title-113")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
