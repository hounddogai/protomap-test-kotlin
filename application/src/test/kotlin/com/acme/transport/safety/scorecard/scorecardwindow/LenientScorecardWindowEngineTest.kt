package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientScorecardWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientScorecardWindowEngine()
        val item = newLenientScorecardWindow("code-81", "title-319")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
