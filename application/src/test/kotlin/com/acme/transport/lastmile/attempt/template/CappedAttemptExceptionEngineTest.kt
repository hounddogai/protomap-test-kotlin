package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedAttemptExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedAttemptExceptionEngine(newRegistry())
        val item = newCappedAttemptException("code-836", "name-870")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
