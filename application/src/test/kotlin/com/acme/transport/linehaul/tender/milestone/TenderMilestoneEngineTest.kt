package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class TenderMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderMilestoneEngine()
        val item = newTenderMilestone("code-298", "label-34")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
