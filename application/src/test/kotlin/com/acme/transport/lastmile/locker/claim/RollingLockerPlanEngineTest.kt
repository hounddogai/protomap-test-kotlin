package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingLockerPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingLockerPlanEngine(newRegistry())
        val item = newRollingLockerPlan("code-497", "title-766")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
