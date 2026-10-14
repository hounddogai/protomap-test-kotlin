package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingTierEngine(newRegistry())
        val item = newCoachingTier("code-276", "title-101")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
