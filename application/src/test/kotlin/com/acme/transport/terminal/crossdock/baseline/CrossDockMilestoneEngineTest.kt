package com.acme.transport.terminal.crossdock.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockMilestoneEngine()
        val item = newCrossDockMilestone("code-917", "label-11")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
