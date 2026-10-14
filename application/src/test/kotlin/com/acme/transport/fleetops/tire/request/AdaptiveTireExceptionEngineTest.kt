package com.acme.transport.fleetops.tire.request

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTireExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveTireExceptionEngine()
        val item = newAdaptiveTireException("code-338", "label-172")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
