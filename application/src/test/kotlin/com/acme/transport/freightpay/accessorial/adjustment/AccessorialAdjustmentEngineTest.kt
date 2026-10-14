package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialAdjustmentEngine()
        val item = newAccessorialAdjustment("code-878", "title-881")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
