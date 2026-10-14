package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveSwapWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveSwapWindowEngine(newRegistry())
        val item = newAdaptiveSwapWindow("code-792", "label-664")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
