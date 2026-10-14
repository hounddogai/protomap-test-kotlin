package com.acme.transport.linehaul.trailer.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTrailerVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTrailerVarianceEngine()
        val item = newLegacyTrailerVariance("code-156", "label-790")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
