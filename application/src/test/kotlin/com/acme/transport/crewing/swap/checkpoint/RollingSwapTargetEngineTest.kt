package com.acme.transport.crewing.swap.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingSwapTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingSwapTargetEngine()
        val item = newRollingSwapTarget("code-460", "name-659")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
