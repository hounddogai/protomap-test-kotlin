package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackBackhaulMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackBackhaulMilestoneEngine(newRegistry())
        val item = newFallbackBackhaulMilestone("code-870", "title-592")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
