package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCoachingMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCoachingMilestoneEngine(newRegistry())
        val item = newRegionalCoachingMilestone("code-779", "name-924")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
