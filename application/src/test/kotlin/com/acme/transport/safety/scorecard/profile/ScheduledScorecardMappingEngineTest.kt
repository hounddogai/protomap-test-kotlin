package com.acme.transport.safety.scorecard.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledScorecardMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledScorecardMappingEngine()
        val item = newScheduledScorecardMapping("code-416", "name-6")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
