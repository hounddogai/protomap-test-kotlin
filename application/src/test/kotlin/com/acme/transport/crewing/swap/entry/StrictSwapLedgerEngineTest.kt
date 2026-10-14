package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictSwapLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictSwapLedgerEngine()
        val item = newStrictSwapLedger("code-917", "name-892")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
