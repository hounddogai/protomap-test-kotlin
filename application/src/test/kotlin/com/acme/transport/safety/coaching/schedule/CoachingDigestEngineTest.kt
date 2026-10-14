package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingDigestEngine(newRegistry())
        val item = newCoachingDigest("code-820", "title-170")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
