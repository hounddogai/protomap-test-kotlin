package com.acme.transport.safety.coaching.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictCoachingExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictCoachingExceptionEngine(newRegistry())
        val item = newStrictCoachingException("code-641", "label-105")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
