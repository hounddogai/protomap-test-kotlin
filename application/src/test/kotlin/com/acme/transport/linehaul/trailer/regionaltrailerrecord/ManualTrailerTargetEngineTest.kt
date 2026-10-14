package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTrailerTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTrailerTargetEngine()
        val item = newManualTrailerTarget("code-621", "name-522")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
