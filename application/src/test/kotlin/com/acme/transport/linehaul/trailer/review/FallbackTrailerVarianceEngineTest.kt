package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackTrailerVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackTrailerVarianceEngine()
        val item = newFallbackTrailerVariance("code-889", "name-943")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
