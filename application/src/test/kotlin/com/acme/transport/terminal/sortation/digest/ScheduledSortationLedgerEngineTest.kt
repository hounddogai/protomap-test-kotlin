package com.acme.transport.terminal.sortation.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledSortationLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledSortationLedgerEngine(newRegistry())
        val item = newScheduledSortationLedger("code-478", "label-421")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
