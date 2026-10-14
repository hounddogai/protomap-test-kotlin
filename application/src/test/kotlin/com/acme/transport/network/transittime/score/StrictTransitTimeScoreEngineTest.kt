package com.acme.transport.network.transittime.score

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTransitTimeScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTransitTimeScoreEngine()
        val item = newStrictTransitTimeScore("code-392", "name-252")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
