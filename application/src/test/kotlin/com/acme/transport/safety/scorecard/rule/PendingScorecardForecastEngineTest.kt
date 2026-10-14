package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingScorecardForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingScorecardForecastEngine(newRegistry())
        val item = newPendingScorecardForecast("code-221", "name-895")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
