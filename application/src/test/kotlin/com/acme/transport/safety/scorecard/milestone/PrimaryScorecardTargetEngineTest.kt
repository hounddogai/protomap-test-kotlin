package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryScorecardTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryScorecardTargetEngine(newRegistry())
        val item = newPrimaryScorecardTarget("code-105", "label-659")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
