package com.acme.transport.linehaul.trailer.trailerrule

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTrailerRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTrailerRuleEngine()
        val item = newBatchedTrailerRule("code-118", "label-203")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
