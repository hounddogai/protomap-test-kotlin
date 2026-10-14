package com.acme.transport.lastmile.locker.lockerrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledLockerRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledLockerRuleEngine(newRegistry())
        val item = newScheduledLockerRule("code-742", "label-846")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
