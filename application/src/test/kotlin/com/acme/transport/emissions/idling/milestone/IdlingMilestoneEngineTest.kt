package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingMilestoneEngine()
        val item = newIdlingMilestone("code-775", "title-393")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
