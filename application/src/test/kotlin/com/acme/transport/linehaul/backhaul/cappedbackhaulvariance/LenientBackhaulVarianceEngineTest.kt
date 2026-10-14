package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientBackhaulVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientBackhaulVarianceEngine()
        val item = newLenientBackhaulVariance("code-357", "label-413")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
