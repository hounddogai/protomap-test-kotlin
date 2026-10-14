package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedCutoffMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedCutoffMilestoneEngine(newRegistry())
        val item = newCappedCutoffMilestone("code-941", "name-488")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
