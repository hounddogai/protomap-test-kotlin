package com.acme.transport.safety.violation.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientViolationExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientViolationExceptionEngine()
        val item = newLenientViolationException("code-613", "title-626")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
