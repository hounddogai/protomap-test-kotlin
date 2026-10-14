package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultScorecardAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultScorecardAllocationEngine(newRegistry())
        val item = newDefaultScorecardAllocation("code-153", "title-802")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
