package com.acme.transport.freightpay.accessorial.request

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedAccessorialTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedAccessorialTemplateEngine()
        val item = newWeightedAccessorialTemplate("code-505", "name-731")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
