package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackTrailerYardBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackTrailerYardBundleEngine()
        val item = newFallbackTrailerYardBundle("code-11", "name-153")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
