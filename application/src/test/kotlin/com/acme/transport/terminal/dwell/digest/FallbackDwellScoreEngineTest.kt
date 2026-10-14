package com.acme.transport.terminal.dwell.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDwellScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDwellScoreEngine()
        val item = newFallbackDwellScore("code-650", "name-640")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
