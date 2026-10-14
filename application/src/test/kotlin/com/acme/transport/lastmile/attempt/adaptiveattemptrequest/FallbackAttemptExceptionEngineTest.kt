package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackAttemptExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackAttemptExceptionEngine(newRegistry())
        val item = newFallbackAttemptException("code-213", "label-40")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
