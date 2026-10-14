package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardLedgerEngine(newRegistry())
        val item = newScorecardLedger("code-816", "title-895")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
