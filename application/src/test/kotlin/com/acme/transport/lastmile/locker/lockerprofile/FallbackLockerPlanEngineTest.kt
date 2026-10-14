package com.acme.transport.lastmile.locker.lockerprofile

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackLockerPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackLockerPlanEngine()
        val item = newFallbackLockerPlan("code-724", "title-742")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
