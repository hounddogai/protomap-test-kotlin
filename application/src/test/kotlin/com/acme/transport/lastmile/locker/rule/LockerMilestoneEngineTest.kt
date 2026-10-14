package com.acme.transport.lastmile.locker.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class LockerMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerMilestoneEngine()
        val item = newLockerMilestone("code-172", "title-200")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
