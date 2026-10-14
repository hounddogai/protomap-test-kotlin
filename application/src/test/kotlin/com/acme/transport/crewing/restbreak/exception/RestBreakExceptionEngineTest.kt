package com.acme.transport.crewing.restbreak.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakExceptionEngine()
        val item = newRestBreakException("code-126", "label-376")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
