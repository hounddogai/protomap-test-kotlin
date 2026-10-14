package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardMilestoneEngine(newRegistry())
        val item = newTrailerYardMilestone("code-2", "name-593")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
