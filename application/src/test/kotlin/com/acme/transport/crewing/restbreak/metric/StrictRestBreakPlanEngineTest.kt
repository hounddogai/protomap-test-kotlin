package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRestBreakPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRestBreakPlanEngine(newRegistry())
        val item = newStrictRestBreakPlan("code-512", "title-1")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
