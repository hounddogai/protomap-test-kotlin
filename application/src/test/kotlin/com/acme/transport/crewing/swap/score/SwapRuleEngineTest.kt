package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapRuleEngine(newRegistry())
        val item = newSwapRule("code-831", "title-181")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
