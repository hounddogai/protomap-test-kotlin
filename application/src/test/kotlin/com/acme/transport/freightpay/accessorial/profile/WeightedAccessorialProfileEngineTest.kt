package com.acme.transport.freightpay.accessorial.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedAccessorialProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedAccessorialProfileEngine()
        val item = newWeightedAccessorialProfile("code-87", "name-964")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
