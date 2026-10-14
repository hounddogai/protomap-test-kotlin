package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualAttemptEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualAttemptEstimateEngine(newRegistry())
        val item = newManualAttemptEstimate("code-438", "label-997")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
