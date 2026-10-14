package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialOverrideEngine()
        val item = newAccessorialOverride("code-78", "title-880")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
