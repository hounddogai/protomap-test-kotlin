package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialEstimateEngine()
        val item = newAccessorialEstimate("code-961", "title-650")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
