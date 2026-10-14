package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerThresholdEngine()
        val item = newTrailerThreshold("code-746", "name-23")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
