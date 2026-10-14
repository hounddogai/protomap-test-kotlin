package com.acme.transport.safety.nearmiss.misscycle

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultNearMissCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultNearMissCycleEngine()
        val item = newDefaultNearMissCycle("code-63", "name-142")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
