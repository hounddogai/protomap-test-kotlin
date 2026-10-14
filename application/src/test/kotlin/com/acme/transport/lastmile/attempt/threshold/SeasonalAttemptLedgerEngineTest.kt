package com.acme.transport.lastmile.attempt.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalAttemptLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalAttemptLedgerEngine()
        val item = newSeasonalAttemptLedger("code-297", "title-111")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
