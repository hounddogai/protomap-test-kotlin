package com.acme.transport.linehaul.trailer.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerTargetEngine()
        val item = newTrailerTarget("code-349", "label-502")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
