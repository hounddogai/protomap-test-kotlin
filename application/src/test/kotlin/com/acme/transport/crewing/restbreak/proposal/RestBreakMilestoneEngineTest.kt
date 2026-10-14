package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakMilestoneEngine(newRegistry())
        val item = newRestBreakMilestone("code-830", "title-383")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
