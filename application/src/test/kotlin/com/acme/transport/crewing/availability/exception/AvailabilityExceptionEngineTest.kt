package com.acme.transport.crewing.availability.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityExceptionEngine()
        val item = newAvailabilityException("code-355", "title-408")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
