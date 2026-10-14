package com.acme.transport.safety.coaching.coachingdigest

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCoachingBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCoachingBudgetEngine()
        val item = newFallbackCoachingBudget("code-602", "label-334")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
