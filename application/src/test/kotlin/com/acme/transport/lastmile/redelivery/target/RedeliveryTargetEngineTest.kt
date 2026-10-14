package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryTargetEngine()
        val item = newRedeliveryTarget("code-716", "name-380")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
