package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyScorecardVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyScorecardVarianceEngine(newRegistry())
        val item = newLegacyScorecardVariance("code-519", "name-643")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
