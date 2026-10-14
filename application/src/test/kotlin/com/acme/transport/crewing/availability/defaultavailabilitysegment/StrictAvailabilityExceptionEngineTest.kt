package com.acme.transport.crewing.availability.defaultavailabilitysegment

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictAvailabilityExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictAvailabilityExceptionEngine()
        val item = newStrictAvailabilityException("code-58", "label-218")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
