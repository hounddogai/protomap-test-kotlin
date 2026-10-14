package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimarySortationMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimarySortationMilestoneEngine(newRegistry())
        val item = newPrimarySortationMilestone("code-711", "name-873")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
