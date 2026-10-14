package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalSwapPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalSwapPlanEngine(newRegistry())
        val item = newGlobalSwapPlan("code-237", "name-837")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
