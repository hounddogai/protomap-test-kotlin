package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedLanePlanWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedLanePlanWindowEngine()
        val item = newWeightedLanePlanWindow("code-418", "name-65")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
