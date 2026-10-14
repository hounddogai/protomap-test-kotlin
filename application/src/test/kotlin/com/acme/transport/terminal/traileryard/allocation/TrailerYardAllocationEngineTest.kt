package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardAllocationEngine(newRegistry())
        val item = newTrailerYardAllocation("code-40", "label-721")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
