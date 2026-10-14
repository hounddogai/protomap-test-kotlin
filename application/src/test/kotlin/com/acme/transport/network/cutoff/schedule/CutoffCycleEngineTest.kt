package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffCycleEngine()
        val item = newCutoffCycle("code-556", "label-527")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
