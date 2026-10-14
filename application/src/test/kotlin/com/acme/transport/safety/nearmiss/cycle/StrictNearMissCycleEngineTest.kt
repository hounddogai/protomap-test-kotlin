package com.acme.transport.safety.nearmiss.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictNearMissCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictNearMissCycleEngine()
        val item = newStrictNearMissCycle("code-579", "title-470")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
