package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptExceptionEngine(newRegistry())
        val item = newAttemptException("code-292", "title-207")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
