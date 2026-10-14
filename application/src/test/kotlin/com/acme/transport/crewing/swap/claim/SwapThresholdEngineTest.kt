package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapThresholdEngine(newRegistry())
        val item = newSwapThreshold("code-22", "title-732")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
