package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackAttemptRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackAttemptRequestEngine()
        val item = newFallbackAttemptRequest("code-934", "label-85")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
