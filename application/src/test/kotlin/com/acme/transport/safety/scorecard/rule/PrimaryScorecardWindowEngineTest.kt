package com.acme.transport.safety.scorecard.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryScorecardWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryScorecardWindowEngine()
        val item = newPrimaryScorecardWindow("code-568", "label-428")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
