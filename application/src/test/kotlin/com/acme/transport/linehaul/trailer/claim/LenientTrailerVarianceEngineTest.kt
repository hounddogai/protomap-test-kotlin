package com.acme.transport.linehaul.trailer.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientTrailerVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientTrailerVarianceEngine()
        val item = newLenientTrailerVariance("code-511", "name-399")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
