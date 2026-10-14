package com.acme.transport.lastmile.redelivery.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryScoreEngine()
        val item = newRedeliveryScore("code-922", "title-184")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
