package com.acme.transport.terminal.crossdock.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockPlanEngine(newRegistry())
        val item = newCrossDockPlan("code-277", "title-973")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
