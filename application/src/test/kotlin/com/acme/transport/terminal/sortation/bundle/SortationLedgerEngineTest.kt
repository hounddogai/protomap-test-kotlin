package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class SortationLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationLedgerEngine()
        val item = newSortationLedger("code-575", "title-185")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
