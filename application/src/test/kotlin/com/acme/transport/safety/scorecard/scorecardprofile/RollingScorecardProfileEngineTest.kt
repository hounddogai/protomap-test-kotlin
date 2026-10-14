package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingScorecardProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingScorecardProfileEngine(newRegistry())
        val item = newRollingScorecardProfile("code-868", "label-665")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
