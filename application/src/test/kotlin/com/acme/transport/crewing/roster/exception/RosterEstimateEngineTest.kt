package com.acme.transport.crewing.roster.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterEstimateEngine()
        val item = newRosterEstimate("code-206", "label-485")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
