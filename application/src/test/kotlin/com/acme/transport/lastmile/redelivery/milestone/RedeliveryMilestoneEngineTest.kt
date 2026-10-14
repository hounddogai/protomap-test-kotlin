package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryMilestoneEngine(newRegistry())
        val item = newRedeliveryMilestone("code-739", "name-982")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
