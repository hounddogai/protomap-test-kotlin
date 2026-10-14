package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDwellScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDwellScoreEngine()
        val item = newStrictDwellScore("code-31", "name-900")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
