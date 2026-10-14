package com.acme.transport.lastmile.drop.target

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDropLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDropLedgerEngine()
        val item = newAdaptiveDropLedger("code-930", "title-345")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
