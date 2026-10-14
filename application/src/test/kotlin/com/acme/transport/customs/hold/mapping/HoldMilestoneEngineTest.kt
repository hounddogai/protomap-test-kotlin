package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldMilestoneEngine(newRegistry())
        val item = newHoldMilestone("code-921", "name-155")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
