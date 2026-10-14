package com.acme.transport.terminal.dockdoor.doorthreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedDockDoorExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedDockDoorExceptionEngine()
        val item = newWeightedDockDoorException("code-816", "title-244")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
