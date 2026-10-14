package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardWindowEngine(newRegistry())
        val item = newScorecardWindow("code-549", "title-280")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
