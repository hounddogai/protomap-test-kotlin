package com.acme.transport.terminal.crossdock.docksummary

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalCrossDockScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalCrossDockScoreEngine()
        val item = newSeasonalCrossDockScore("code-817", "label-279")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
