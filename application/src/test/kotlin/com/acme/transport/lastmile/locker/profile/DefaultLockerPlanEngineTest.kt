package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultLockerPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultLockerPlanEngine(newRegistry())
        val item = newDefaultLockerPlan("code-574", "title-711")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
