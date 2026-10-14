package com.acme.transport.terminal.dwell.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDwellDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDwellDraftEngine()
        val item = newCappedDwellDraft("code-734", "name-58")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
