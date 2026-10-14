package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissCycleEngine()
        val item = newNearMissCycle("code-461", "label-415")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
