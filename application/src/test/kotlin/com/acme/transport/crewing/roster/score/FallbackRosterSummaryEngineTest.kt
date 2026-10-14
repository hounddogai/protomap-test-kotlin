package com.acme.transport.crewing.roster.score

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRosterSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRosterSummaryEngine()
        val item = newFallbackRosterSummary("code-349", "label-801")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
