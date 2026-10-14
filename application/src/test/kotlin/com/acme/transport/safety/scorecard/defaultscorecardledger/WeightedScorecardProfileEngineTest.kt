package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedScorecardProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedScorecardProfileEngine(newRegistry())
        val item = newWeightedScorecardProfile("code-234", "label-427")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
