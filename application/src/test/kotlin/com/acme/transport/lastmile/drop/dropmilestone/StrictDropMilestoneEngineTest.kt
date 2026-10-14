package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDropMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDropMilestoneEngine()
        val item = newStrictDropMilestone("code-776", "name-332")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
