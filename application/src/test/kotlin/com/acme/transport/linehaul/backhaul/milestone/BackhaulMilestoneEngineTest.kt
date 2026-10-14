package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulMilestoneEngine(newRegistry())
        val item = newBackhaulMilestone("code-573", "label-294")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
