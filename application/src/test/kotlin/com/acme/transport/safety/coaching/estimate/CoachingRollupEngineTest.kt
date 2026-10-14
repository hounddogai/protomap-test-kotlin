package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingRollupEngine(newRegistry())
        val item = newCoachingRollup("code-328", "title-210")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
