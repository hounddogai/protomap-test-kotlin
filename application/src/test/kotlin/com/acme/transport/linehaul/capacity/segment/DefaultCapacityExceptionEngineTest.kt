package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCapacityExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultCapacityExceptionEngine()
        val item = newDefaultCapacityException("code-368", "name-930")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
