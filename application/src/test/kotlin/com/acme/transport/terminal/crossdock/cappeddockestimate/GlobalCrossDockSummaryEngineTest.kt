package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCrossDockSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCrossDockSummaryEngine()
        val item = newGlobalCrossDockSummary("code-476", "title-622")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
