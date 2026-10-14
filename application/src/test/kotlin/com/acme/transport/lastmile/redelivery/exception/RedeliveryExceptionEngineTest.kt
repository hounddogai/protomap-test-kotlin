package com.acme.transport.lastmile.redelivery.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryExceptionEngine()
        val item = newRedeliveryException("code-343", "title-713")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
