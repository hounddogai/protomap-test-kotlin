package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingClaimEngine(newRegistry())
        val item = newCoachingClaim("code-403", "title-706")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
