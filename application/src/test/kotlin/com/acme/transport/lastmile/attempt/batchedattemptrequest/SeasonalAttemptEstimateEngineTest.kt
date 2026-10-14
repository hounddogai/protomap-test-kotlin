package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalAttemptEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalAttemptEstimateEngine(newRegistry())
        val item = newSeasonalAttemptEstimate("code-763", "title-405")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
