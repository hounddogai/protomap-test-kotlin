package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRedeliveryRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRedeliveryRuleEngine()
        val item = newScheduledRedeliveryRule("code-157", "name-569")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
