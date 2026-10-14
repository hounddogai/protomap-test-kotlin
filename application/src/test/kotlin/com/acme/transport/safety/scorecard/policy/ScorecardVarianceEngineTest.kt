package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardVarianceEngine(newRegistry())
        val item = newScorecardVariance("code-969", "title-588")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
