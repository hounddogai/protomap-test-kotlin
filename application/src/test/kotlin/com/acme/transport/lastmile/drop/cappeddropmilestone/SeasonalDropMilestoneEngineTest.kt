package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDropMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDropMilestoneEngine()
        val item = newSeasonalDropMilestone("code-834", "label-292")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
