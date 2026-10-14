package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedServiceAreaExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedServiceAreaExceptionEngine()
        val item = newBatchedServiceAreaException("code-486", "title-570")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
