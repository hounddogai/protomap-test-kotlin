package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyCrossDockSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyCrossDockSummaryEngine()
        val item = newLegacyCrossDockSummary("code-883", "title-264")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
