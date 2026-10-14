package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingCoachingScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingCoachingScoreEngine(newRegistry())
        val item = newRollingCoachingScore("code-908", "name-276")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
