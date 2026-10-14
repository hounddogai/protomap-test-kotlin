package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDockDoorScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDockDoorScoreEngine()
        val item = newSeasonalDockDoorScore("code-720", "name-595")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
