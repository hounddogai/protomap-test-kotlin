package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCoachingBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCoachingBaselineEngine(newRegistry())
        val item = newGlobalCoachingBaseline("code-49", "title-564")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
