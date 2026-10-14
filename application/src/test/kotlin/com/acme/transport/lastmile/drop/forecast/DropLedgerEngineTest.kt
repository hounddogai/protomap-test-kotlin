package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class DropLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropLedgerEngine()
        val item = newDropLedger("code-30", "name-982")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
