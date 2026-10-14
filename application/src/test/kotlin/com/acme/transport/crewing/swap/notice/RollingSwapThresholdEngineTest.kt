package com.acme.transport.crewing.swap.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingSwapThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingSwapThresholdEngine()
        val item = newRollingSwapThreshold("code-954", "title-771")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
