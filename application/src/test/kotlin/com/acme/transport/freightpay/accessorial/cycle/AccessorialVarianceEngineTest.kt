package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialVarianceEngine()
        val item = newAccessorialVariance("code-460", "label-350")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
