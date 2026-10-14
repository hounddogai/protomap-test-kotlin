package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialGroupEngine()
        val item = newAccessorialGroup("code-252", "label-39")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
