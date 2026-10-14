package com.acme.transport.freightpay.accessorial.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialProfileEngine()
        val item = newAccessorialProfile("code-219", "name-459")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
