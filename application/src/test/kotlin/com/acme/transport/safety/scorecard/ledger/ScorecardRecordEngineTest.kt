package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardRecordEngine(newRegistry())
        val item = newScorecardRecord("code-850", "name-971")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
